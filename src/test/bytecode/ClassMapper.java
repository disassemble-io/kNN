import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;

import java.io.FileWriter;
import java.nio.file.Files;

/**
 * @author Tyler Sedlar
 * @since 6/11/16
 */
public class ClassMapper {

    private static final String LOG = "115.gson";
    private static final String OUT_FILE = "115-MAP.gson";

    @Test
    public void logToMap() throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject root = new JsonParser()
                .parse(new String(Files.readAllBytes(Resources.path(LOG))))
                .getAsJsonObject();
        JsonObject classes = root.getAsJsonObject("classes");
        JsonObject newJSON = new JsonObject();
        classes.entrySet().forEach(entry -> newJSON.add(
                entry.getValue().getAsJsonObject().get("name").getAsString(), gson.toJsonTree(entry.getKey())
        ));
        try (FileWriter output = new FileWriter(Resources.path(OUT_FILE).toFile())) {
            gson.toJson(newJSON, output);
        }
    }
}
