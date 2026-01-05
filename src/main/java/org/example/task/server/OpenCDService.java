package org.example.task.server;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OpenCDService {
    private static final Logger logger = LoggerFactory.getLogger(OpenCDService.class);
    public static String loginToQbit(String url, String username, String password) throws Exception {
        String sessionId = ""; // 用于存储 SID
        HttpURLConnection connection = (HttpURLConnection) new URL(url + "/api/v2/auth/login").openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String loginData = "username=" + URLEncoder.encode(username, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8");

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = loginData.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // 读取响应头并提取 Session ID
        String cookies = connection.getHeaderField("Set-Cookie");
        if (cookies != null && cookies.contains("SID=")) {
            sessionId = cookies.split(";")[0].split("=")[1];
            logger.info("登录成功，Session ID: {}", sessionId);
        } else {
            logger.error("登录失败，未获取到 Session ID");
        }

        int status = connection.getResponseCode();
        if (status == 200) {
            logger.debug("登录成功！");
        } else {
            logger.debug("登录失败，状态码: {}", status);
        }
        return sessionId;
    }

    public static int getDownloadingCount(String sessionId, String qbUrl)throws Exception { {
        if (sessionId.isEmpty()) {
            logger.error("请先登录 qBittorrent！");
            return 0;
        }

        // 创建连接
        HttpURLConnection connection = (HttpURLConnection) new URL(qbUrl + "/api/v2/torrents/info").openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Cookie", "SID=" + sessionId); // 在请求中添加 SID

        int status = connection.getResponseCode();
        if (status != 200) {
            logger.error("获取任务列表失败，状态码: {}", status);
            return 0;
        }

        // 读取响应数据
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        // 解析 JSON 响应，使用 FastJSON 解析
        JSONArray torrents = JSONArray.parseArray(response.toString());
        int downloadingCount = 0;

        // 遍历所有任务，查找正在下载的任务
        for (int i = 0; i < torrents.size(); i++) {
            JSONObject torrent = torrents.getJSONObject(i);
            String state = torrent.getString("state");

            // 如果任务的状态为 "downloading"，则计数
            if ("downloading".equals(state)) {
                downloadingCount++;
            }
        }
        logger.info("当前下载器正在下载 {} 个任务", downloadingCount);
        return downloadingCount;
    }
}

    public static List<String> fetchDownloadLinks(String cookie, String passkey, int incldead, int optionTorrents, int spstate, int maxNum) {
        String searchUrl = "https://open.cd/torrents.php?inclbookmarked=0" +
                "&incldead=" + incldead +
                "&spstate=" + spstate +
                "&option-torrents=" + optionTorrents +
                "&page=0&sort=7&type=desc";
        List<String> downloadUrls = new ArrayList<>();

        try {
            // 每次请求前随机等待 0~30 秒
            int sleepTime = new Random().nextInt(30000);
            Thread.sleep(sleepTime);

            // 连接页面并获取文档
            Document doc = Jsoup.connect(searchUrl)
                    .cookie("cookie", cookie)
                    .get();

            // 选择所有的 <a> 标签
            Elements elements = doc.select("a");
            //选择的下载链接数量
            int num = 0;
            for (Element element : elements) {
                String hrefStr = element.attr("href");
                if (hrefStr.contains("download.php?id=")) {
                    // 获取下载链接的 ID，并打印出完整的下载链接
                    String id = hrefStr.replace("download.php?id=", "").trim();
                    String downloadUrl = "https://open.cd/download.php?id=" + id + "&passkey=" + passkey;
                    downloadUrls.add(downloadUrl);
                    num++;
                }
                if (maxNum == num) {
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            // 捕获异常并打印堆栈信息
            e.printStackTrace();
        }
        return downloadUrls;
    }


    public static void addMagnetLinks(List<String> magnetLinks, String sessionId, String qbUrl, String savePath, String category , String tag) throws Exception {
        //循环添加新种子
        for (String magnetLink : magnetLinks) {
            logger.info("添加种子 : {}",magnetLink.substring(29,magnetLink.length()-41));
            addMagnetLink(sessionId,qbUrl, magnetLink, savePath, category, tag);
        }
    }

    // 向 qBittorrent 添加磁力链接，并设置下载目录和分类
    public static void addMagnetLink(String sessionId, String url, String magnetLink, String savePath, String category, String tag) throws Exception {
        // 如果没有 Session ID，就不继续
        if (sessionId.isEmpty()) {
            System.out.println("请先登录 qBittorrent！");
            return;
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(url + "/api/v2/torrents/add").openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Cookie", "SID=" + sessionId); // 在请求中添加 SID

        // 设置请求数据，注意需要进行 URL 编码
        String data = "urls=" + URLEncoder.encode(magnetLink, "UTF-8") +
                "&savepath=" + URLEncoder.encode(savePath, "UTF-8") +
                "&category=" + URLEncoder.encode(category, "UTF-8") +
                "&tags=" + URLEncoder.encode(tag, "UTF-8");

        // 发送请求数据
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = data.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // 获取响应
        int status = connection.getResponseCode();
        if (status == 200) {
//            logger.info("磁力链接已成功添加，下载路径:{} , 分类:{}", savePath, category);
        } else {
            logger.error("添加磁力链接失败，状态码: {}", status);
        }
    }
}
