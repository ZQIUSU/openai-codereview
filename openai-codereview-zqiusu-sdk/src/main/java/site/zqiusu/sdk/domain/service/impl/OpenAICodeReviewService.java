package site.zqiusu.sdk.domain.service.impl;

import org.checkerframework.checker.units.qual.C;
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

    public OpenAICodeReviewService(GitCommand gitCommand, IOpenAI openAI, WeiXin weiXin) {
        super(gitCommand, openAI, weiXin);
    }

    @Override
    protected String getDiffCode() throws IOException, InterruptedException {
        return gitCommand.diff();
    }

    @Override
    protected String codeReview(String diffCode) throws Exception {

        ChatCompletionRequestDTO chatCompletionRequest = new ChatCompletionRequestDTO();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());

        ArrayList<ChatCompletionRequestDTO.Prompt> message = new ArrayList<>();
        message.add(new ChatCompletionRequestDTO.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"));
        message.add(new ChatCompletionRequestDTO.Prompt("user",diffCode));
        chatCompletionRequest.setMessages(message);

        ChatCompletionSyncResponseDTO completions = openAI.completions(chatCompletionRequest);
        ChatCompletionSyncResponseDTO.Message lastMessage = completions.getChoices().get(0).getMessage();

        return lastMessage.getContent();
    }

    @Override
    protected String recordCodeReview(String recommend) throws GitAPIException, IOException {
        return gitCommand.commitAndPush(recommend);
    }

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
