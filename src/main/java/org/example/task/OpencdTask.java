package org.example.task;

import org.example.task.server.OpenCDService;
import org.example.utils.YamlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class OpencdTask implements Runnable {

    private static final Logger logger =
            LoggerFactory.getLogger(OpencdTask.class);

    // 读取并 flatten YAML
    Map<String, String> config = YamlUtils.flatten(YamlUtils.loadYaml("application.yml"));

    // ==== QB 配置 ====
    String qbUrl = config.get("qb.url");
    String qbUsername = config.get("qb.username");
    String qbPassword = config.get("qb.password");
    String savePath = config.get("qb.path");
    String category = config.get("qb.category");
    String tag = config.get("qb.tag");
    int qbMaxNum = Integer.parseInt(config.get("qb.maxNum"));

    // ==== Opencd 配置 ====
    String opencdCookie = config.get("opencd.cookie");
    String opencdPasskey = config.get("opencd.passkey");

    // ==== Torrents 配置 ====
    int torrentsIncldead = Integer.parseInt(config.get("torrents.incldead"));
    int torrentsOptionTorrents = Integer.parseInt(config.get("torrents.optionTorrents"));
    int torrentsSpstate = Integer.parseInt(config.get("torrents.spstate"));

    @Override
    public void run() {
        if (2==torrentsOptionTorrents){
            logger.info("当前配置选择 [免费、下载过、但当前未做种] 的资源");
        }else if (6==torrentsOptionTorrents){
            logger.info("当前配置选择 [免费、未下载过] 的资源");
        }

        try {
            logger.info("============ 执行 opencd 定时任务 ============ ");

            String sessionId = OpenCDService.loginToQbit(qbUrl, qbUsername, qbPassword);

            int downloadingCount = OpenCDService.getDownloadingCount(sessionId,qbUrl);

            if (downloadingCount >= 10){
                logger.info("当前下载数 {} , 不添加新任务",downloadingCount);
            }else {
                logger.info("本次任务设置下载器最大下载数为 {} , 当前下载数 {} , 本次计划添加 {} 个任务", qbMaxNum, downloadingCount, qbMaxNum - downloadingCount);

                logger.info("搜索做种数最多的符合条件的 {} 个种子",qbMaxNum - downloadingCount);
                List<String> magnetLinks = OpenCDService.fetchDownloadLinks(opencdCookie, opencdPasskey, torrentsIncldead, torrentsOptionTorrents, torrentsSpstate, qbMaxNum-downloadingCount);
                logger.info("找到符合条件的 {} 个种子",magnetLinks.size());
                if (magnetLinks.isEmpty()){
                    logger.info("本次不添加种子到下载器");
                }else {
                    OpenCDService.addMagnetLinks(magnetLinks,sessionId,qbUrl, savePath, category, tag);
                }
            }
        } catch (Exception e) {
            logger.error("Opencd 任务执行失败", e);
        }
    }
}