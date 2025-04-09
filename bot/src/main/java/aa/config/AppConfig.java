package aa.config;

import lombok.Data;

@Data
public class AppConfig {
    private DataSourceConfig datasource;
    private JpaConfig jpa;
    private FlywayConfig flyway;
    private Settings settings = new Settings();
    private TelegramSettings telegram = new TelegramSettings();

    @Data
    public static class DataSourceConfig {
        private String driver;
        private String url;
        private String username;
        private String password;
    }

    @Data
    public static class JpaConfig {
        private String dialect;
        private String default_schema;
        private String ddl_auto;
    }

    @Data
    public static class FlywayConfig {
        private boolean enabled;
        private String url;
        private String user;
        private String password;
        private String schemas;
        private String table;
        private String locations;
    }

    @Data
    public static class Settings {
        private String threshold;
    }

    @Data
    public static class TelegramSettings {
        private String token;
    }
}
