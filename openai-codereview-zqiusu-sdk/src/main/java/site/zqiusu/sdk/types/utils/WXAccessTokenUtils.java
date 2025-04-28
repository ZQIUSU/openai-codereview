package site.zqiusu.sdk.types.utils;

import com.alibaba.fastjson2.JSON;
import lombok.Data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WXAccessTokenUtils {

    private static final String APPID ="wx5c2005a3a8b211de";
    private static final String APPSECRET ="b6315d5f262488e39e4238371dc4846c";
    private static final String GRANT_TYPE = "client_credential";
    private static final String URL_TEMPLATE = "https://api.weixin.qq.com/cgi-bin/token?grant_type=%s&appid=%s&secret=%s";

    public static String getAccessToken(){
        return getAccessToken(APPID,APPSECRET);
    }

    public static String getAccessToken(String appId,String appSecret){
        try{
            String urlString = String.format(URL_TEMPLATE,GRANT_TYPE,appId,appSecret);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder sb = new StringBuilder();

                while ((line = in.readLine()) != null){
                    sb.append(line);
                }
                in.close();


                System.out.println("response: "+sb);
                Token token = JSON.parseObject(sb.toString(), Token.class);
                return token.getAccess_token();
            }else {
                System.out.println("GET request failed");
                return null;
            }

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Data
    public static class Token {
        private String access_token;
        private Integer expires_in;
    }
}
