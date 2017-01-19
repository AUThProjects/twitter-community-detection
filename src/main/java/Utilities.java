import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by steve on 19/01/2017.
 */
public class Utilities {
    public static Properties loadProperties(String path) {
        Properties toBeReturned = new Properties();
        InputStream inputStream = null;


        try {
            inputStream = Utilities.class.getClassLoader().getResourceAsStream(path);

            if (inputStream != null) {
                toBeReturned.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + path + "' not found in the classpath");
            }

        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                System.err.println("Exception: " + e.getMessage());
            }
        }
        return toBeReturned;
    }

    public static void main(String[] args) {
        Properties config = loadProperties("config.properties");
        for (Map.Entry<Object,Object> p : config.entrySet()) {
            System.out.printf("%s: %s\n", (String)p.getKey(), (String)p.getValue());
        }
    }
}
