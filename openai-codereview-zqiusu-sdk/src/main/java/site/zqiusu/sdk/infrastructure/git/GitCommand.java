package site.zqiusu.sdk.infrastructure.git;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Data
public class GitCommand {
    private final Logger logger = LoggerFactory.getLogger(GitCommand.class);

    private final String githubReviewLogUri;

    private final String githubToken;

    private final String project;

    private final String branch;

    private final String author;

    private final String message;

    public String diff() throws IOException, InterruptedException {
        ProcessBuilder logProcessBuilder = new ProcessBuilder("git", "log", "-1", "--pretty==format=%H");
        logProcessBuilder.directory(new File("."));
        Process logProcess = logProcessBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(logProcess.getInputStream()));
        String latestCommitHash = reader.readLine();
        reader.close();
        logProcess.waitFor();
    }
}
