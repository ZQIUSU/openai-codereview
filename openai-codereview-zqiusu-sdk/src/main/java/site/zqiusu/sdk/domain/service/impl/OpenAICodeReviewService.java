package site.zqiusu.sdk.domain.service.impl;

import org.eclipse.jgit.api.errors.GitAPIException;
import site.zqiusu.sdk.domain.model.Model;
import site.zqiusu.sdk.domain.service.AbstractOpenAICodeReviewService;
import site.zqiusu.sdk.infrastructure.git.GitCommand;
import site.zqiusu.sdk.infrastructure.openai.IOpenAI;
import site.zqiusu.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import site.zqiusu.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;
import site.zqiusu.sdk.infrastructure.weixin.WeiXin;
import site.zqiusu.sdk.infrastructure.weixin.dto.TemplateMessageDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OpenAICodeReviewService extends AbstractOpenAICodeReviewService {

    //super调用父类的构造函数
    public OpenAICodeReviewService(GitCommand gitCommand, IOpenAI openAI, WeiXin weiXin) {
        super(gitCommand, openAI, weiXin);
    }

    //这里的检出代码操作和infrastructure里实现的检出操作一模一样，所以直接调用diff方法就好了
    @Override
    protected String getDiffCode() throws IOException, InterruptedException {
        return gitCommand.diff();
    }

    //infrastructure里的代码审计功能需要一个入参，在这里实现了，所以这里就是DTO的构造和调用OpenAI的执行方法
    @Override
    protected String codeReview(String diffCode) throws Exception {

        //构造入参
        ChatCompletionRequestDTO chatCompletionRequest = new ChatCompletionRequestDTO();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        //这里用ArrayList来实现List接口
        ArrayList<ChatCompletionRequestDTO.Prompt> message = new ArrayList<>();
        message.add(new ChatCompletionRequestDTO.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，" +
                "请您根据git diff记录，对代码做出评审。代码如下:"));
        message.add(new ChatCompletionRequestDTO.Prompt("user",diffCode));
        chatCompletionRequest.setMessages(message);

        //接收返回值，取出其中的message的content内容，传给下一个方法做入参
        ChatCompletionSyncResponseDTO completions = openAI.completions(chatCompletionRequest);
        ChatCompletionSyncResponseDTO.Message lastMessage = completions.getChoices().get(0).getMessage();

        return lastMessage.getContent();
    }

    //将上一个评论写入github日志仓库，需要提前创建仓库，然后返回日志的地址，地址也有用，到时候地址就放在公众号推送模板里，点击就能访问，但是github有防火墙，记得科学上网
    @Override
    protected String recordCodeReview(String recommend) throws GitAPIException, IOException {
        return gitCommand.commitAndPush(recommend);
    }

    //将url和一些项目的信息，信息构造到模板的DTO对象里，直接能看到，url放到
    @Override
    protected void pushMessage(String logUrl) throws Exception {
        Map<String , Map<String ,String >> data = new HashMap<>();
        TemplateMessageDTO.put(data,TemplateMessageDTO.TemplateKey.REPO_NAME, gitCommand.getProject());
        TemplateMessageDTO.put(data,TemplateMessageDTO.TemplateKey.BRANCH_NAME, gitCommand.getBranch());
        TemplateMessageDTO.put(data,TemplateMessageDTO.TemplateKey.COMMIT_AUTHOR, gitCommand.getAuthor());
        TemplateMessageDTO.put(data,TemplateMessageDTO.TemplateKey.COMMIT_MESSAGE, gitCommand.getMessage());
        System.out.println(data);
        weiXin.sendTemplateMessage(logUrl,data);
    }

}
