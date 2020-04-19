package com.wangboak.resetdns.main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Description
 * <p>
 * </p>
 * DATE 2018/1/21.
 * @author WangBo.
 */
public class HttpClient {

    public static String get(String url) {
        try {
            URL client = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) client.openConnection();

            // set timeout
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setDoOutput(true);

            conn.connect();
            int statusCode = conn.getResponseCode();
            InputStream is = conn.getInputStream();
            if (is != null) {
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
                is.close();
                byte[] bytes = outStream.toByteArray();
                return new String(bytes, "UTF-8");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取当前IP.
     * 从淘宝的API获取，服务稳定性上更具有保障。
     */
    public static String getCurrentIP() {
        String s = get("http://ip.taobao.com/service/getIpInfo.php?ip=myip");

        String keyWord = "{\"ip\":\"";

        int start = s.indexOf(keyWord);
        int end = s.indexOf("\",\"", start);

        String substring = s.substring(start + keyWord.length(), end);

        return substring;
    }

    public static void main(String[] args) {
        System.out.println(getCurrentIP());
    }
}
