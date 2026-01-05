package org.example.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class YamlUtils {

    private static final Logger logger =
            LoggerFactory.getLogger(YamlUtils.class);
    /**
     * 优先从外部路径读取配置文件
     */
    public static Map<String, Object> loadYaml(String path) {
        try (InputStream in = new FileInputStream(path)) {
            return new Yaml().load(in);
        } catch (FileNotFoundException e) {

            if (System.getProperty("os.name").toLowerCase().contains("linux")){
                throw new RuntimeException("配置文件不存在: " + path);
            }else {
                return loadYamlFromClasspath(path);
            }
        } catch (Exception e) {
            logger.error("配置文件读取失败: {}", path);
            throw new RuntimeException("读取配置文件失败: " + path, e);
        }
    }
    /**
     * 作为兜底：从 jar 内 classpath 读取
     */
    public static Map<String, Object> loadYamlFromClasspath(String name) {
        try (InputStream in = YamlUtils.class
                .getClassLoader()
                .getResourceAsStream(name)) {

            if (in == null) {
                throw new RuntimeException("classpath 中找不到配置文件: " + name);
            }
            return new Yaml().load(in);
        } catch (Exception e) {
            throw new RuntimeException("读取 classpath 配置失败", e);
        }
    }

    /**
     * 把多层 Map 拉平成：qb.maxNum 这种形式
     */
    public static Map<String, String> flatten(Map<String, Object> source) {
        Map<String, String> result = new HashMap<>();
        flatten("", source, result);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static void flatten(String prefix,
                                Map<String, Object> source,
                                Map<String, String> result) {

        source.forEach((key, value) -> {
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

            if (value instanceof Map) {
                flatten(fullKey, (Map<String, Object>) value, result);
            } else {
                result.put(fullKey, String.valueOf(value));
            }
        });
    }
}