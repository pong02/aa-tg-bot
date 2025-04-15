package aa.scheduler;

import aa.repository.LabelDao;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.*;

@Slf4j
public class LabelPurgeScheduler {

    private final LabelDao labelDao;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private LocalDate lastRunDate = null;

    public LabelPurgeScheduler(LabelDao labelDao) {
        this.labelDao = labelDao;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::checkAndPurge, 0, 30, TimeUnit.SECONDS);
    }

    private void checkAndPurge() {
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();

        if (now.getHour() == 0 && now.getMinute() == 0) {
            if (!today.equals(lastRunDate)) {
                try {
                    int count = labelDao.purgeDeleted();
                    lastRunDate = today;
                    System.out.println("Purged " + count + " soft-deleted labels at 00:00.");
                } catch (Exception e) {
                    log.error("Error during scheduled purge", e);
                }
            }
        }
    }
}
