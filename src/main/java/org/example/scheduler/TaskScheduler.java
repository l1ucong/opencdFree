package org.example.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.example.task.OpencdTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskScheduler {

    private static final Logger logger =
            LoggerFactory.getLogger(TaskScheduler.class);

    private static final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    public static void start(long intervalSeconds) {

        scheduler.scheduleAtFixedRate(
                new OpencdTask(),
                30, // 首次延迟 = 间隔时间
                intervalSeconds,
                TimeUnit.SECONDS
        );

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("关闭定时任务");
            scheduler.shutdown();
        }));
    }
}