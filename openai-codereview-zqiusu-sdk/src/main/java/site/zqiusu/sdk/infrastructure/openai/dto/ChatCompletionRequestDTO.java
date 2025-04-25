package site.zqiusu.sdk.infrastructure.openai.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class ChatCompletionRequestDTO {

    private String model;
    private List<Prompt> messages;

    @Data
    @AllArgsConstructor
    public static class Prompt {
        private String role;
        private String content;
    }

}
