package aa.helper;

import aa.config.AppConfig;

import lombok.Getter;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    @Getter
    private static SessionFactory sessionFactory;

    public static void init(AppConfig config) {
        Configuration cfg = new Configuration();

        AppConfig.DataSourceConfig ds = config.getDatasource();
        AppConfig.JpaConfig jpa = config.getJpa();

        cfg.setProperty("hibernate.connection.driver_class", ds.getDriver());
        cfg.setProperty("hibernate.connection.url", ds.getUrl());
        cfg.setProperty("hibernate.connection.username", ds.getUsername());
        cfg.setProperty("hibernate.connection.password", ds.getPassword());

        cfg.setProperty("hibernate.dialect", jpa.getDialect());
        cfg.setProperty("hibernate.default_schema", jpa.getDefault_schema());
        cfg.setProperty("hibernate.hbm2ddl.auto", jpa.getDdl_auto());

        cfg.addAnnotatedClass(aa.model.Envelope.class);
        cfg.addAnnotatedClass(aa.model.Stamp.class);
        cfg.addAnnotatedClass(aa.model.StampConfiguration.class);
        cfg.addAnnotatedClass(aa.model.StampCombination.class);

        sessionFactory = cfg.buildSessionFactory(
                new StandardServiceRegistryBuilder().applySettings(cfg.getProperties()).build()
        );
    }

}
