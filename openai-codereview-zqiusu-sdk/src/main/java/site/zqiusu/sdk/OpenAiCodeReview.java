package site.zqiusu.sdk;


import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.zqiusu.sdk.domain.service.impl.OpenAICodeReviewService;
import site.zqiusu.sdk.infrastructure.git.GitCommand;
import site.zqiusu.sdk.infrastructure.openai.IOpenAI;
import site.zqiusu.sdk.infrastructure.openai.impl.ChatGLM;
import site.zqiusu.sdk.infrastructure.weixin.WeiXin;

public class OpenAiCodeReview {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiCodeReview.class);

    //配置
    private String weixin_appid = "wx5c2005a3a8b211de";
    private String weixin_secret = "b6315d5f262488e39e4238371dc4846c";
    private String weixin_touser = "omADk6tkNC439hXDMlSgf-3ESlrU";
    private String weixin_template_id = "zNFIcuyQUntcHQTXFbFD655gKuAdAIEcmLtXMXskyDQ";

    //chatGLM配置
    private String chatglm_host = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private String chatglm_apikeysecret = "";

    // Github 配置
    private String github_review_log_uri;
    private String github_token;

    // 工程配置 - 自动获取
    private String github_project;
    private String github_branch;
    private String github_author;


    public static void main(String[] args) throws Exception {
        GitCommand gitCommand = new GitCommand(
                getEnv("GITHUB_REVIEW_LOG_URI"),
                getEnv("GITHUB_TOKEN"),
                getEnv("COMMIT_PROJECT"),
                getEnv("COMMIT_BRANCH"),
                getEnv("COMMIT_AUTHOR"),
                getEnv("COMMIT_MESSAGE")
        );


        WeiXin weiXin = new WeiXin(
                getEnv("WEIXIN_APPID"),
                getEnv("WEIXIN_SECRET"),
                getEnv("WEIXIN_TOUSER"),
                getEnv("WEIXIN_TEMPLATE_ID")
        );

        IOpenAI openAI = new ChatGLM(getEnv("CHATGLM_APIHOST"),getEnv("CHATGLM_APIKEYSECRET"));
        OpenAICodeReviewService openAICodeReviewService = new OpenAICodeReviewService(gitCommand,openAI,weiXin);
        openAICodeReviewService.exec();
    }


    private static String getEnv(String key) {
        String value = System.getenv(key);
        if (null == value || value.isEmpty()) {
            throw new RuntimeException("value is null");
        }
        return value;
    }
}
