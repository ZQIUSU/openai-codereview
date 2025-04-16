package site.zqiusu.sdk.model;

import lombok.*;

import java.util.List;

@Data
public class ChatCompletionRequest {

    private String model = Model.GLM_4_FLASH.getCode();
    private List<Prompt> messages;


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Prompt {
        private String role;
        private String content;


    }

}
