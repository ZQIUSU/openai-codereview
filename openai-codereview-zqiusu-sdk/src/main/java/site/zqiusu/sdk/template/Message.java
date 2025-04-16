package site.zqiusu.sdk.template;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Message {
    private String touser = "omADk6tkNC439hXDMlSgf-3ESlrU";
    private String template_id = "zNFIcuyQUntcHQTXFbFD655gKuAdAIEcmLtXMXskyDQ";
    private String url = "https://github.com/ZQIUSU/openai-codereview-log/blob/main/2025-04-15/EACOCVWNCBAP.md";

    private Map<String ,Map<String ,String >> data = new HashMap<>();

    public void put (String key ,String value){
        data.put(key,new HashMap<String ,String >() {
            {
                put("value",value);
            }
        });
    }


}
