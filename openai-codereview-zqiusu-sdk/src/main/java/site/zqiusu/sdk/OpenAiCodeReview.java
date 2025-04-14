package site.zqiusu.sdk;

import com.alibaba.fastjson2.JSON;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import site.zqiusu.sdk.model.ChatCompletionRequest;
import site.zqiusu.sdk.model.ChatCompletionSyncResponse;
import site.zqiusu.sdk.model.Model;
import site.zqiusu.sdk.types.utils.BearerTokenUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.logging.SimpleFormatter;


public class OpenAiCodeReview {
    public static void main(String[] args) throws Exception {
        System.out.println("测试执行");

        //1.代码检出
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        processBuilder.directory(new File("."));

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        StringBuilder diffCode = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            diffCode.append(line);
        }

        int exitCode = process.waitFor();
        System.out.println("Exited with code:" + exitCode);

        System.out.println("评审代码：" + diffCode.toString());

        String log = codeReview(diffCode.toString());
        System.out.println("评审结果 ："+log);

    }

    public static String writeLog(String token,String log) throws Exception {
        //1.克隆github仓库到本地仓库
        Git.cloneRepository()
                .setURI("").setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(token,""))
                .call();

        //2.获取当前日期，并创建文件夹,注意java.util.Date是java7之前的包，不是线程安全的，最好用java8的java.time包
        String dateFolder = new SimpleDateFormat("yyyy-mm-dd").format(new Date());

        //3.生成随机文件名，创建文件
        String fileName = generateFileName(12)+".md";
        //file的第一个入参是父文件夹，第二个入参是子文件
        File file = new File(dateFolder,fileName);
        //try后（）里的内容声明需要自动关闭的资源
        try (FileWriter fileWriter = new FileWriter(file)){

        }
        return "";
    }

    private static String generateFileName(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(characters.charAt(random.nextInt(characters.length())));
        }
        return stringBuilder.toString();

    }

    private static String codeReview(String diffCode)throws Exception{
        String apiSecret = "9b454e3977beb45e1a747e1d2605d7c1.2oyyDnaQrhqtEUVM";
        String token = BearerTokenUtils.getToken(apiSecret);

        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization","Bearer "+token);
        connection.setRequestProperty("Content-Type","application/json");
        connection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);



        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequest.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;
            {
                add(new ChatCompletionRequest.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"));
                add(new ChatCompletionRequest.Prompt("user", diffCode));
            }
        });


        try(OutputStream os = connection.getOutputStream()){
//            byte[] input = jsonInpuString.getBytes(StandardCharsets.UTF_8);
            byte[] input = JSON.toJSONString(chatCompletionRequest).getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }

        int responseCode = connection.getResponseCode();
        System.out.println("状态码为:" + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;

        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null){
            content.append(inputLine);
        }

        in.close();
        connection.disconnect();

        ChatCompletionSyncResponse response = JSON.parseObject(content.toString(), ChatCompletionSyncResponse.class);
        return response.getChoices().get(0).getMessage().getContent();

    }
}
