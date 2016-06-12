import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Tyler Sedlar
 * @since 6/11/16
 */
public class Resources {

    public static Path path(String path) throws URISyntaxException {
        return Paths.get("src/test/resources/").resolve(path);
    }
}
