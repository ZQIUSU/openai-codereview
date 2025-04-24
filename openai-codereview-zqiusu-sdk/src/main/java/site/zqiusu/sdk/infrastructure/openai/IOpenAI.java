package site.zqiusu.sdk.infrastructure.openai;

import site.zqiusu.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import site.zqiusu.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;

import java.net.MalformedURLException;

public interface IOpenAI {
    ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO requestDTO) throws Exception;
}
