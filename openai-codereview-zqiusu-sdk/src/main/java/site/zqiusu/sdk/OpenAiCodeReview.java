package site.zqiusu.sdk;


import site.zqiusu.sdk.domain.service.impl.OpenAICodeReviewService;
import site.zqiusu.sdk.infrastructure.git.GitCommand;
import site.zqiusu.sdk.infrastructure.openai.IOpenAI;
import site.zqiusu.sdk.infrastructure.openai.impl.ChatGLM;
import site.zqiusu.sdk.infrastructure.weixin.WeiXin;

public class OpenAiCodeReview {
    public static void main(String[] args) throws Exception {
        //初始化GitCommand对象
        GitCommand gitCommand = new GitCommand(
                getEnv("COMMIT_MESSAGE"),
                getEnv("COMMIT_AUTHOR"),
                getEnv("COMMIT_BRANCH"),
                getEnv("COMMIT_PROJECT"),
                getEnv("GITHUB_TOKEN"),
                getEnv("GITHUB_REVIEW_LOG_URI")
        );

        //初始化WeiXin对象
        WeiXin weiXin = new WeiXin(
                getEnv("WEIXIN_APPID"),
                getEnv("WEIXIN_SECRET"),
                getEnv("WEIXIN_TOUSER"),
                getEnv("WEIXIN_TEMPLATE_ID")
        );

        //初始化IOpenAI对象
        IOpenAI openAI = new ChatGLM(getEnv("CHATGLM_APIHOST"),getEnv("CHATGLM_APIKEYSECRET"));

        //构造这个服务
        OpenAICodeReviewService openAICodeReviewService = new OpenAICodeReviewService(gitCommand,openAI,weiXin);
        openAICodeReviewService.exec();
    }

    //获取java程序的环境变量，因为已经通过yml文件写入到java环境里了
    private static String getEnv(String key) {
        String value = System.getenv(key);
        if (null == value || value.isEmpty()) {
            throw new RuntimeException("value is null");
        }
        return value;
    }
}
