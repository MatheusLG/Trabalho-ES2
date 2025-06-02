package dem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
@RestController
@RequestMapping("/dem")
public class DemMicroservice {

    private static final String API_URL = "https://restcountries.com/v3.1/all";
    private static final int LIMIT = 30;
    private static final String Data_Path = "../../../Data/";
    private static final String OUTPUT_FILE = Data_Path + "countries_limited.json";
    private static final String FILTERED_FILE = Data_Path + "countries_filtered.json";

    public static void main(String[] args) {
        System.out.println("oi");
        SpringApplication.run(DemMicroservice.class, args);
    }

    @GetMapping("/extract")
    public String extractCountries() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            List<JsonNode> countries = mapper.readValue(response.body(), new TypeReference<List<JsonNode>>() {
            });
            List<JsonNode> limitedCountries = countries.subList(0, Math.min(LIMIT, countries.size()));

            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(new File(OUTPUT_FILE), limitedCountries);

            return "Extração concluída com sucesso. Arquivo gerado: " + OUTPUT_FILE;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Erro durante a extração: " + e.getMessage();
        }
    }

    // filter
    @GetMapping("/filter")
    public String filterCountries() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new File(OUTPUT_FILE));

            if (!root.isArray()) {
                return "Erro: O arquivo de entrada não é uma lista de países.";
            }

            ArrayNode filteredArray = mapper.createArrayNode();

            for (JsonNode country : root) {
                ObjectNode filtered = mapper.createObjectNode();

                JsonNode nameNode = country.path("name");
                filtered.put("Country", nameNode.isTextual() ? nameNode.asText() : "");
                filtered.put("Official Name", nameNode.path("official").asText(""));

                JsonNode capitalArray = country.path("capital");
                filtered.put("Capital",
                        capitalArray.isArray() && capitalArray.size() > 0 ? capitalArray.get(0).asText() : "");

                filtered.put("Region", country.path("region").asText(""));
                filtered.put("Subregion", country.path("subregion").asText(""));

                JsonNode languages = country.path("languages");
                String firstLanguage = "";
                if (languages.isObject()) {
                    Iterator<String> keys = languages.fieldNames();
                    if (keys.hasNext()) {
                        firstLanguage = languages.path(keys.next()).asText("");
                    }
                }
                filtered.put("Official Language", firstLanguage);

                filtered.put("Independent", country.path("independent").asBoolean(false));
                filtered.put("Population", country.path("population").asLong(0));
                filtered.put("Area", country.path("area").asDouble(0.0));

                JsonNode borders = country.path("borders");
                StringBuilder borderStr = new StringBuilder();
                if (borders.isArray()) {
                    for (int i = 0; i < borders.size(); i++) {
                        borderStr.append(borders.get(i).asText());
                        if (i < borders.size() - 1)
                            borderStr.append(", ");
                    }
                }
                filtered.put("Borders", borderStr.toString());

                JsonNode currencies = country.path("currencies");
                String currencyName = "";
                if (currencies.isObject()) {
                    Iterator<String> keys = currencies.fieldNames();
                    if (keys.hasNext()) {
                        String key = keys.next();
                        currencyName = currencies.path(key).path("name").asText("");
                    }
                }
                filtered.put("Currency", currencyName);

                JsonNode timezones = country.path("timezones");
                filtered.put("Time Zone", timezones.isArray() && timezones.size() > 0 ? timezones.get(0).asText() : "");

                filtered.put("Driving Side", country.path("car").path("side").asText(""));
                filtered.put("UN Member", country.path("unMember").asBoolean(false));
                filtered.put("Google Maps", country.path("maps").path("googleMaps").asText(""));
                filtered.put("Flag", country.path("flag").asText(""));

                filteredArray.add(filtered);
            }

            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(new File(FILTERED_FILE), filteredArray);

            return "Filtro aplicado com sucesso. Arquivo gerado: " + FILTERED_FILE;

        } catch (IOException e) {
            e.printStackTrace();
            return "Erro durante o filtro: " + e.getMessage();
        }
    }

    // pasta para mdm (Crud)
    // dentro da classe DemMicroservice

    @GetMapping("/sendToMDM")
    public String sendFilteredToMDM() {
        try {
            String jsonData = Files.readString(Paths.get(FILTERED_FILE));
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/countries/bulk")) // ajuste a URL do seu MDM
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return "Envio para MDM concluído com status: " + response.statusCode();

        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao enviar para MDM: " + e.getMessage();
        }
    }

}
