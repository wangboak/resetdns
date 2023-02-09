package com.wangboak.resetdns.main;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsRequest;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsResponse;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordRequest;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author wangbo
 * @date 2017/7/14
 */
public class Main {

    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static IAcsClient client = null;

    /**
     *  必填固定值，必须为“cn-hangzhou”
     */
    private static final String regionId = "cn-hangzhou";


    static private Config config = null;

    public static void main(String[] args) throws Exception {
        while (true) {
            try {
                Config c2 = getConfig();
                if (config == null || !config.equals(c2)) {
                    config = c2;
                    IClientProfile profile = DefaultProfile.getProfile(regionId, config.getAccessKey(), config.getSecretKey());
                    client = new DefaultAcsClient(profile);
                }

                String currentIp = HttpClient.getCurrentIP();

                Map<String, DescribeDomainRecordsResponse.Record> records = getRecords(config.getDomain());

                Set<String> rrs = records.keySet();
                for (String rr : rrs) {
                    DescribeDomainRecordsResponse.Record record = records.get(rr);

                    String recordId = record.getRecordId();
                    String rrname = record.getRR();
                    String ip = record.getValue();

                    if (rrname.equalsIgnoreCase(config.getHostname())) {
                        System.out.println(currentTime() + "当前真实IP：[" + currentIp + "]， " + config.getHostname() + "的A记录为：[" + ip + "]");
                        if (!ip.equalsIgnoreCase(currentIp)) {
                            updateADomainIp(recordId, rrname, currentIp);
                            System.out.println(currentTime() + "IP不一致，已经更新DNS记录");
                        }
                    }
                }

                // sleep 10 分钟
                Thread.sleep(1000 * 60 * 10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * 更新主机名的A记录IP
     * @param recordId 记录ID
     * @param RR 主机名
     * @param ip IP地址
     */
    public static void updateADomainIp(String recordId, String RR, String ip) {
        UpdateDomainRecordRequest request = new UpdateDomainRecordRequest();

        try {
            request.setRecordId(recordId);
            request.setType("A");
            request.setRR(RR);
            request.setValue(ip);

            UpdateDomainRecordResponse res = client.getAcsResponse(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取所有记录信息
     * @return
     */
    public static Map<String, DescribeDomainRecordsResponse.Record> getRecords(String domain) {

        DescribeDomainRecordsRequest rrr = new DescribeDomainRecordsRequest();

        Map<String, DescribeDomainRecordsResponse.Record> RRMap = new HashMap<>();

        try {
            rrr.setDomainName(domain);

            DescribeDomainRecordsResponse acsResponse = client.getAcsResponse(rrr);
            List<DescribeDomainRecordsResponse.Record> domainRecords = acsResponse.getDomainRecords();
            for (DescribeDomainRecordsResponse.Record r : domainRecords) {
                RRMap.put(r.getRecordId(), r);
            }
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return RRMap;
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
        String path = System.getProperty("file");

        File file = new File(path);

        Properties p = new Properties();
        p.load(new FileReader(file));


        Config config = new Config();
        config.setAccessKey(p.getProperty("accessKey"));
        config.setSecretKey(p.getProperty("secretKey"));
        config.setHostname(p.getProperty("hostname"));
        config.setDomain(p.getProperty("domain"));

        return config;
    }
}
