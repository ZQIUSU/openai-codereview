package site.zqiusu.sdk.test;

import org.junit.Test;
import site.zqiusu.sdk.types.utils.BearerTokenUtils;

import java.io.IOException;


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
        System.out.println("bbbb");
        System.out.println("cccc才对");
    }
}
