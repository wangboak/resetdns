package com.wangboak.resetdns.main;

import java.io.File;
import java.io.FileReader;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

import com.aliyun.alidns20150109.Client;
import com.aliyun.alidns20150109.models.DescribeDomainRecordsRequest;
import com.aliyun.alidns20150109.models.DescribeDomainRecordsResponse;
import com.aliyun.alidns20150109.models.DescribeDomainRecordsResponseBody;
import com.aliyun.alidns20150109.models.UpdateDomainRecordRequest;
import com.aliyun.alidns20150109.models.UpdateDomainRecordResponse;

/**
 *
 * @author wangbo
 * @date 2017/7/14
 */
public class Main {

    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static Client client = null;
    static private Config config = null;

    static {
        try {
            config = getConfig();
            client = createClient();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        while (System.currentTimeMillis() > 0) {
            try {
                // 检查并更新IP
                checkAndUpdateIp();
                // sleep 10 分钟
                Thread.sleep(1000 * 60 * 10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查并更新IP
     * @throws Exception
     */
    private static void checkAndUpdateIp() throws Exception {
        String currentIp = getCurrentIP();
        DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord record = getRecords(config);
        String value = record.getValue();

        if (currentIp.equals(value)) {
            System.out.println(currentTime() + " [" + currentIp + "] IP地址没有变化，不需要更新。");
        } else {
            updateADomainIp(record, currentIp);
        }
    }

    /**
     * 更新主机名的A记录IP
     */
    public static void updateADomainIp(DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord record,
            String newIp) {
        UpdateDomainRecordRequest request = new UpdateDomainRecordRequest();

        try {
            request.setRecordId(record.getRecordId());
            request.setType("A");
            request.setRR(record.getRR());
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
    public static DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord getRecords(Config config) throws Exception {
        Client client = createClient();

        DescribeDomainRecordsRequest req = new DescribeDomainRecordsRequest();
        req.setDomainName(config.getDomain());
        req.setRRKeyWord(config.getHostname());
        req.setType("A");

        DescribeDomainRecordsResponse resp = client.describeDomainRecords(req);
        DescribeDomainRecordsResponseBody data = resp.getBody();

        DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecords records =
                data.getDomainRecords();

        List<DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord> record = records.getRecord();

        return record.get(0);
    }

    public static String currentTime() {
        return LocalDateTime.now(ZoneId.of("GMT+8")).format(formatter);
    }

    /**
     * 获取配置文件内容
     * @return
     * @throws Exception
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
        String s = HttpClient.get("http://icanhazip.com");
        if (s != null && !s.isEmpty()) {
            return s.trim();
        }
        return s;
    }
}
