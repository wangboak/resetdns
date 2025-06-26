package com.wangboak.resetdns.main;

import java.io.File;
import java.io.FileReader;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.aliyun.alidns20150109.Client;
import com.aliyun.alidns20150109.models.DescribeDomainRecordInfoRequest;
import com.aliyun.alidns20150109.models.DescribeDomainRecordInfoResponse;
import com.aliyun.alidns20150109.models.DescribeDomainRecordsRequest;
import com.aliyun.alidns20150109.models.DescribeDomainRecordsResponse;
import com.aliyun.alidns20150109.models.DescribeDomainRecordsResponseBody;
import com.aliyun.alidns20150109.models.UpdateDomainRecordRequest;
import com.aliyun.alidns20150109.models.UpdateDomainRecordResponse;

import lombok.Data;

/**
 *
 * @author wangbo
 * @date 2017/7/14
 */
public class Main {

    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static Client client = null;
    private static Config config = null;

    static String recordId = null;

    private static final long UPDATE_INTERVAL = 1000 * 60 * 60; // 1 小时

    private static Map<String, RecordCacheDTO> cacheMap = new HashMap<>();

    static {
        try {
            config = getConfig();
            client = createClient();
            recordId = getRecordId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 1000 * 1000; i++) {
            try {
                // 检查并更新IP
                checkAndUpdateIp();
                // sleep 3 分钟
                Thread.sleep(1000 * 60 * 3);// 3分钟 检测一次。
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查并更新IP
     * @throws Exception
     */
    private static void checkAndUpdateIp() {
        try {
            String realIp = getCurrentIP();
            String value = getRecordIp(config);

            if (realIp.equals(value)) {
                System.out.println(currentTime() + " [" + realIp + "] IP地址没有变化，不需要更新。");
            } else {
                updateADomainIp(recordId, "A", config.getHostname(), realIp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新主机名的A记录IP
     */
    public static void updateADomainIp(String recordId, String type, String hostName, String newIp) {
        UpdateDomainRecordRequest request = new UpdateDomainRecordRequest();

        try {
            request.setRecordId(recordId);
            request.setType(type);
            request.setRR(hostName);
            request.setValue(newIp);
            UpdateDomainRecordResponse resp = client.updateDomainRecord(request);
            System.out.println(currentTime() + " 更新成功, IP：[" + newIp + "]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取 对应hostname 对应的A记录。
     * @return 返回对应的A记录
     */
    public static String getRecordIp(Config config) throws Exception {
        String hostname = config.getHostname() + "." + config.getDomain();
        RecordCacheDTO cacheDTO = cacheMap.get(hostname);
        if (cacheDTO != null) {
            if (System.currentTimeMillis() - cacheDTO.getUpdateTime() < TimeUnit.MINUTES.toMillis(59)) {
                return cacheDTO.getIp();
            }
        }

        DescribeDomainRecordInfoResponse response = client.describeDomainRecordInfo(
                new DescribeDomainRecordInfoRequest().setRecordId(recordId)
        );

        String ip = response.getBody().getValue();

        {
            RecordCacheDTO recordCache = new RecordCacheDTO();
            recordCache.setIp(ip);
            recordCache.setUpdateTime(System.currentTimeMillis());
            cacheMap.put(hostname, recordCache);
        }

        return ip;
    }

    /**
     * 获取记录ID
     * @return 返回记录ID
     * @throws Exception 异常
     */
    private static String getRecordId() throws Exception {
        Client client = createClient();
        DescribeDomainRecordsRequest req = new DescribeDomainRecordsRequest();
        req.setDomainName(config.getDomain());
        req.setRRKeyWord(config.getHostname());
        req.setType("A");

        DescribeDomainRecordsResponse resp = client.describeDomainRecords(req);
        DescribeDomainRecordsResponseBody data = resp.getBody();
        DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecords records =
                data.getDomainRecords();
        List<DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord> recordList =
                records.getRecord();
        DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord record = recordList.get(0);
        recordId = record.getRecordId();
        return recordId;
    }

    public static String currentTime() {
        return LocalDateTime.now(ZoneId.of("GMT+8")).format(formatter);
    }

    /**
     * 获取配置文件内容
     * @return 返回配置文件内容
     * @throws Exception 异常
     */
    public static Config getConfig() throws Exception {

        String filePath = getFilePath();

        File file = new File(filePath + "/resetdns.conf");

        Properties p = new Properties();
        p.load(new FileReader(file));


        Config config = new Config();
        config.setAccessKey(p.getProperty("accessKey"));
        config.setSecretKey(p.getProperty("secretKey"));
        config.setHostname(p.getProperty("hostname"));
        config.setDomain(p.getProperty("domain"));

        return config;
    }

    public static String getFilePath() {
        String jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            jarPath = URLDecoder.decode(jarPath, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        File jarFile = new File(jarPath);
        String jarDir = jarFile.getParent();
        System.out.println("JAR 文件所在目录：" + jarDir);
        return jarDir;
    }

    /**
     * 创建阿里云DNS客户端
     * @return
     */
    public static com.aliyun.alidns20150109.Client createClient() {
        if (client != null) {
            return client;
        }

        String accessKey = config.getAccessKey();
        String secretKey = config.getSecretKey();

        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(accessKey)
                .setAccessKeySecret(secretKey);
        config.endpoint = "alidns.cn-hangzhou.aliyuncs.com";

        try {
            client = new Client(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return client;
    }

    /**
     * 获取当前IP.
     * 从淘宝的API获取，服务稳定性上更具有保障。
     */
    public static String getCurrentIP() {
        // 参考： https://api.ipify.org, https://ddns.oray.com/checkip, https://ip.3322.net, https://4.ipw.cn, https://v4.yinghualuo.cn/bejson
        String s = HttpClient.get("https://4.ipw.cn");
        if (s != null && !s.isEmpty()) {
            return s.trim();
        }
        return s;
    }

    @Data
    private static class RecordCacheDTO {
        private String ip;
        private Long updateTime;
    }
}
