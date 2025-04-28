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
        ProcessBuilder logProcessBuilder = new ProcessBuilder("git", "log", "-1", "--pretty=format:%H");
        logProcessBuilder.directory(new File("."));
        Process logProcess = logProcessBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(logProcess.getInputStream()));
        String latestCommitHash = reader.readLine();
        reader.close();
        logProcess.waitFor();

        ProcessBuilder diffProcessBuilder = new ProcessBuilder("git", "diff", latestCommitHash + "^", latestCommitHash);
        diffProcessBuilder.directory(new File("."));
        Process diffProcess = diffProcessBuilder.start();

        StringBuilder diffCode = new StringBuilder();
        BufferedReader diffReader = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()));
        String line;
        while ((line = diffReader.readLine()) != null) {
            diffCode.append(line).append("\n");
        }
        diffReader.close();

        int exitCode = diffProcess.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to diff, exit code:" + exitCode);
        }

        return diffCode.toString();

    }

    //提交并推送
    public String commitAndPush(String recommend) throws GitAPIException, IOException {\
        //先把远程仓库克隆到本地仓库
        Git git = Git.cloneRepository()
                .setURI(githubReviewLogUri+".git")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken,""))
                .call();

        //创建分支
        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateFolder = new File("repo/" + dateFolderName);
        if(!dateFolder.exists()){
            dateFolder.mkdirs();
        }

        String fileName =project + "-" +branch+"-"+author+System.currentTimeMillis()+".md";
        File newFile  = new File(dateFolder,fileName);
        try(FileWriter writer = new FileWriter(newFile)){
            writer.write(recommend);
        };

        //提交内容
        git.add().addFilepattern(dateFolderName + "/" + fileName);
        git.commit().setMessage("add code review new file" + fileName).call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken,"")).call();

        logger.info("openai-code-review git commit and push done! {}", fileName);

        return githubReviewLogUri + "/tree/main/" + dateFolderName + "/" + fileName;
    }
}
