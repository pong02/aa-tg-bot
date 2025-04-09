package aa.config;

import org.flywaydb.core.Flyway;

public class FlywayMigrator {
    public static void migrate(AppConfig.FlywayConfig config) {
        Flyway.configure()
                .dataSource(config.getUrl(), config.getUser(), config.getPassword())
                .schemas(config.getSchemas())
                .table(config.getTable())
                .locations(config.getLocations())
                .load()
                .migrate();
    }
}
