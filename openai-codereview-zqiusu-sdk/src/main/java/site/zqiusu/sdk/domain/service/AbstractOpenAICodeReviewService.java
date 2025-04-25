package site.zqiusu.sdk.domain.service;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.zqiusu.sdk.infrastructure.git.GitCommand;
import site.zqiusu.sdk.infrastructure.openai.IOpenAI;
import site.zqiusu.sdk.infrastructure.weixin.WeiXin;

import java.io.IOException;

public abstract class AbstractOpenAICodeReviewService implements IOpenAICodeReviewService{
    private final Logger logger = LoggerFactory.getLogger(AbstractOpenAICodeReviewService.class);

    protected final GitCommand gitCommand;
    protected final IOpenAI openAI;
    protected final WeiXin weiXin;

    public AbstractOpenAICodeReviewService(GitCommand gitCommand, IOpenAI openAI, WeiXin weiXin) {
        this.gitCommand = gitCommand;
        this.openAI = openAI;
        this.weiXin = weiXin;
    }

    @Override
    public void exec(){
        try{
            //1.获取提交代码
            String diffCode =getDiffCode();
            //2.评审代码
            String recommend = codeReview(diffCode);
            //3.写日志结果，返回日志地址
            String logUrl=recordCodeReview(recommend);
            //4.发送消息通知
            pushMessage(logUrl);
        }catch (Exception e){
            logger.error("openai-code-review-error:",e);
        }
    }

    protected abstract void pushMessage(String logUrl);

    protected abstract String recordCodeReview(String recommend) throws GitAPIException, IOException;

    protected abstract String codeReview(String diffCode) throws Exception;

    protected abstract String getDiffCode() throws IOException, InterruptedException;
}
