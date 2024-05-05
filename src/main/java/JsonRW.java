import com.google.gson.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonRW {
    private static final Logger logger = Logger.getLogger("json");
    private static HashMap<String, ArrayList<String>> read(String filePath) {
        HashMap<String, ArrayList<String>> hashMap = new HashMap<>();
        try {
            FileReader fileReader = new FileReader(filePath);
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(fileReader);
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                // Iterate over the entries
                for (String key : jsonObject.keySet()) {
                    JsonElement value = jsonObject.get(key);
                    hashMap.put(key, new ArrayList<>());
                    // Check if the value is a JsonArray
                    if (value.isJsonArray()) {
                        JsonArray jsonArray = value.getAsJsonArray();

                        // Iterate over the array
                        for (JsonElement element : jsonArray) {
                            // Check if the element is a JsonPrimitive (string)
                            if (element.isJsonPrimitive()) {
                                String stringValue = element.getAsString();
                                hashMap.get(key).add(stringValue);
                            }
                        }
                    }
                }

            } else {
                System.out.println("Unexpected JSON structure. Expected an array.");
            }
            fileReader.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        } catch (JsonParseException e) {
            logger.log(Level.INFO, e.getMessage());
        }
        return hashMap;
    }
    private static void write(Object o, String outputFile) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(o);
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(json);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }
}
