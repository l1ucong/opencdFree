package org.example.launcher;

import java.util.Map;
import org.example.scheduler.TaskScheduler;
import org.example.utils.YamlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppLauncher {
    private static final Logger logger = LoggerFactory.getLogger(AppLauncher.class);

    public static void start(String[] args) {

        String configPath = "application.yml"; // 默认当前目录

        // 解析命令行参数
        for (String arg : args) {
            if (arg.startsWith("--config=")) {
                configPath = arg.substring("--config=".length());
            }
        }

        Map<String, Object> yaml  = YamlUtils.loadYaml(configPath);
        logger.info("使用外部配置文件: {}", configPath);

        Map<String, String> config = YamlUtils.flatten(yaml);

        // 启动检查
        logger.info("==== 项目启动配置检查 ====");
        config.forEach((k, v) -> logger.info("{} = {}", k, v));
        logger.info("==== 配置检查结束 ====");

        int intervalSeconds = Integer.parseInt(
                config.getOrDefault("task.intervalSeconds", "300")
        );

        TaskScheduler.start(intervalSeconds);
    }
}