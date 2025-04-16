package site.zqiusu.sdk.test;

import com.alibaba.fastjson2.JSON;
import org.junit.Test;
import site.zqiusu.sdk.template.Message;
import site.zqiusu.sdk.types.utils.WXAccessTokenUtils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import static site.zqiusu.sdk.types.utils.WXAccessTokenUtils.getAccessToken;

public class TemplateTest {
    @Test
    public void test(){
        String accessToken = getAccessToken();
        System.out.println(accessToken);

        Message message = new Message();
        message.put("time", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        message.put("message","评审结果请点击");

        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken);
        sendPostRequest(url, JSON.toJSONString(message));

    }

    private static void sendPostRequest(String urlString, String jsonBody) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
                String response = scanner.useDelimiter("\\A").next();
                System.out.println(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
