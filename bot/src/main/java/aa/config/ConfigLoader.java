package aa.config;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.LoaderOptions;
import java.io.InputStream;

public class ConfigLoader {
    public static AppConfig load() {
        LoaderOptions options = new LoaderOptions();
        Constructor constructor = new Constructor(AppConfig.class, options);
        Yaml yaml = new Yaml(constructor);

        InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("application.yml");
        if (input == null) {
            throw new RuntimeException("application.yml not found in classpath");
        }

        return yaml.load(input);
    }
}
