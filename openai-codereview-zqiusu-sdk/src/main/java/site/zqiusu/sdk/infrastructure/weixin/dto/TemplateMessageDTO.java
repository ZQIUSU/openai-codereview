package site.zqiusu.sdk.infrastructure.weixin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Data
public class TemplateMessageDTO {
    private String touser = "omADk6tkNC439hXDMlSgf-3ESlrU";
    private String template_id = "zNFIcuyQUntcHQTXFbFD655gKuAdAIEcmLtXMXskyDQ";
    private String url = "https://github.com/ZQIUSU/openai-codereview-log/blob/main/2025-04-15/EACOCVWNCBAP.md";
    private Map<String, Map<String, String>> data = new HashMap<>();

    public static void put(Map<String,Map<String ,String >> data, TemplateKey key,String value){
        data.put(key.getCode(),new HashMap<String ,String >() {
            {
                put("value",value);
            }
        });
    }

    @Getter
    @AllArgsConstructor
    public enum TemplateKey{
        REPO_NAME("repo_name","仓库名称"),
        BRANCH_NAME("branch_name","分支名称"),
        COMMIT_AUTHOR("commit_author","提交作者"),
        COMMIT_MESSAGE("commit_message","提交信息"),
        ;

        private final String code;
        private final String desc;


    }


}
