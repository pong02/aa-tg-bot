package aa;

import aa.config.AppConfig;
import aa.helper.HibernateUtil;
import aa.config.ConfigLoader;
import aa.config.FlywayMigrator;
import aa.entity.Bot;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import java.util.Arrays;

@Slf4j
public class Main {

    public static void main(String[] args) {
        AppConfig config = ConfigLoader.load();

        if (config.getFlyway().isEnabled()) {
            FlywayMigrator.migrate(config.getFlyway());
        }

        HibernateUtil.init(config);

        String botToken = config.getTelegram().getToken();
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, new Bot(botToken));
            System.out.println("Waltuh successfully started!");
            Thread.currentThread().join();
        } catch (Exception e) {
            log.info(Arrays.toString(e.getStackTrace()));
        }
    }
}