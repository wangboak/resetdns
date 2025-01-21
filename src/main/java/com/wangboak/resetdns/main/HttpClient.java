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
}
