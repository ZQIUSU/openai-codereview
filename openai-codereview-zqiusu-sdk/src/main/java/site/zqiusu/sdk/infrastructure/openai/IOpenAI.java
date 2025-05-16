package site.zqiusu.sdk.infrastructure.openai;

import site.zqiusu.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import site.zqiusu.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;

public interface IOpenAI {
    ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO requestDTO) throws Exception;
}
