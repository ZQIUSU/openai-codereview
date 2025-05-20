package site.zqiusu.sdk.infrastructure.git;

import lombok.Data;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class GitCommand {
    //日志
    private final Logger logger = LoggerFactory.getLogger(GitCommand.class);
    //log项目的url
    private final String githubReviewLogUri;
    //gitHub action的token
    private final String githubToken;
    //项目名称
    private final String project;
    //分支名称
    private final String branch;
    //作者
    private final String author;
    //消息
    private final String message;

    public GitCommand(String message, String author, String branch, String project, String githubToken, String githubReviewLogUri) {
        this.message = message;
        this.author = author;
        this.branch = branch;
        this.project = project;
        this.githubToken = githubToken;
        this.githubReviewLogUri = githubReviewLogUri;
    }

    //检出代码
    public String diff() throws IOException, InterruptedException {

        //git log -1 操作表示查看最近的一次提交，后边是格式化输出，意思是只输出最后一次提交的哈希值
        ProcessBuilder logProcessBuilder = new ProcessBuilder("git", "log", "-1", "--pretty=format:%H");
        logProcessBuilder.directory(new File("."));
        Process logProcess = logProcessBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(logProcess.getInputStream()));
        String latestCommitHash = reader.readLine();
        reader.close();
        //等待这个操作进行完再进行下列操作
        logProcess.waitFor();

        //检出最近两次提交代码的差异
        ProcessBuilder diffProcessBuilder = new ProcessBuilder("git", "diff", latestCommitHash + "^", latestCommitHash);
        diffProcessBuilder.directory(new File("."));
        Process diffProcess = diffProcessBuilder.start();

        //因为这个输出肯定不止一行，就用StringBuilder来构造一下
        StringBuilder diffCode = new StringBuilder();
        BufferedReader diffReader = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()));
        String line;
        while ((line = diffReader.readLine()) != null) {
            diffCode.append(line).append("\n");
        }
        diffReader.close();

        //这个是判断异常码的，如果正常返回就会返回0
        int exitCode = diffProcess.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to diff, exit code:" + exitCode);
        }

        return diffCode.toString();

    }

    //提交并推送
    public String commitAndPush(String recommend) throws GitAPIException, IOException {
        //先把远程仓库克隆到本地仓库
        Git git = Git.cloneRepository()
                .setURI(githubReviewLogUri+".git")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken,""))
                .call();

        //创建一个文件夹，用于存放当天的代码评审结果，如果文件夹存在，不做操作，不存在再创建，目的是如果一天有多个提交，防止创建重复文件夹
        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateFolder = new File("repo/" + dateFolderName);
        if(!dateFolder.exists()){
            dateFolder.mkdirs();
        }

        //在文件夹下创建文件
        String fileName =project + "-" +branch+"-"+author+System.currentTimeMillis()+".md";
        File newFile  = new File(dateFolder,fileName);
        //try-with-source 用于自动管理资源，避免内存泄漏
        try(FileWriter writer = new FileWriter(newFile)){
            writer.write(recommend);
        };

        //提交内容，当时上课时候听傅哥说这个是模拟提交代码的操作，也就是本地提交
        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
        //写提交的message
        git.commit().setMessage("add code review new file" + fileName).call();
        //推送
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken,"")).call();

        logger.info("openai-code-review git commit and push done! {}", fileName);

         //返回拼接后的url，直接具体到文件
        return githubReviewLogUri + "/tree/main/" + dateFolderName + "/" + fileName;
    }
}
