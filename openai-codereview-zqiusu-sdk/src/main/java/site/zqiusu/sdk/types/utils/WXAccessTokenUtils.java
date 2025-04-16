package site.zqiusu.sdk.types.utils;

import com.alibaba.fastjson2.JSON;

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
        try{
            String urlString = String.format(URL_TEMPLATE,GRANT_TYPE,APPID,APPSECRET);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            if(responseCode == HttpURLConnection.HTTP_OK){
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder builder = new StringBuilder();

                while ((line = in.readLine()) != null){
                    builder.append(line);
                }
                in.close();


                System.out.println("response: "+builder);
                Token token = JSON.parseObject(builder.toString(), Token.class);
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

    public static class Token {
        private String access_token;
        private Integer expires_in;

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public Integer getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(Integer expires_in) {
            this.expires_in = expires_in;
        }
    }
}
