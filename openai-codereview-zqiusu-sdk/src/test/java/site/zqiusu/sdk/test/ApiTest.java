package site.zqiusu.sdk.test;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.transport.http.HttpConnection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import site.zqiusu.sdk.model.ChatCompletionSyncResponse;
import site.zqiusu.sdk.types.utils.BearerTokenUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;


public class ApiTest {
    @Test
    public void test(){
        String apiSecret = "9b454e3977beb45e1a747e1d2605d7c1.2oyyDnaQrhqtEUVM";
        String token = BearerTokenUtils.getToken(apiSecret);
        System.out.println(token);
    }

    @Test
    public void http_test() throws IOException {
        System.out.println("aaaa");
    }
}
