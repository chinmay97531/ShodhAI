package ai.shodhai.judge.service;

import ai.shodhai.judge.domain.Problem;
import ai.shodhai.judge.domain.Submission;
import ai.shodhai.judge.domain.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class JudgeExecutionService {

    private static final Logger log = LoggerFactory.getLogger(JudgeExecutionService.class);

    private final boolean dockerEnabled;
    private final String dockerImage;

    public JudgeExecutionService(@Value("${judge.docker.enabled:false}") boolean dockerEnabled,
                                 @Value("${judge.docker.image:shodhai/judge:latest}") String dockerImage) {
        this.dockerEnabled = dockerEnabled;
        this.dockerImage = dockerImage;
    }

    public ExecutionSummary judgeSubmission(Submission submission) {
        Problem problem = submission.getProblem();
        List<TestCase> testCases = problem.getTestCases();
        if (testCases.isEmpty()) {
            return ExecutionSummary.accepted(0.0, "No test cases configured");
        }

        double totalTime = 0.0;
        int passed = 0;

        for (TestCase testCase : testCases) {
            ExecutionOutcome outcome = runSingleTest(submission, testCase);
            if (!outcome.success()) {
                String verdict = Optional.ofNullable(outcome.verdict()).orElse("Wrong Answer");
                return ExecutionSummary.failed(verdict, totalTime, outcome.message());
            }
            totalTime += outcome.timeSeconds();
            passed++;
        }

        double score = passed == testCases.size() ? 1.0 : (double) passed / testCases.size();
        return ExecutionSummary.accepted(totalTime, "Accepted", score);
    }

    private ExecutionOutcome runSingleTest(Submission submission, TestCase testCase) {
        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("submission-");
            LanguageRuntime runtime = resolveRuntime(submission.getLanguage());
            if (runtime == LanguageRuntime.UNSUPPORTED) {
                return ExecutionOutcome.failure("Unsupported Language", 0.0, "Language " + submission.getLanguage() + " is not supported yet.");
            }

            Path sourceFile = workDir.resolve(runtime.sourceFile());
            Files.writeString(sourceFile, submission.getSourceCode(), StandardCharsets.UTF_8);

            String input = Optional.ofNullable(testCase.getInputData()).orElse("");
            ExecutionOutcome outcome;
            if (dockerEnabled) {
                outcome = execute(runtime.dockerCommand(workDir, dockerImage), workDir, input, false);
                if (!outcome.success() && "System Error".equalsIgnoreCase(outcome.verdict())) {
                    log.warn("Docker execution failed, falling back to local runtime: {}", outcome.message());
                    outcome = execute(runtime.localCommand(), workDir, input, true);
                }
            } else {
                outcome = execute(runtime.localCommand(), workDir, input, true);
            }

            if (!outcome.success()) {
                return outcome;
            }

            String expected = Optional.ofNullable(testCase.getExpectedOutput()).orElse("").trim();
            if (!normalizeLineEndings(outcome.output()).equals(normalizeLineEndings(expected))) {
                return ExecutionOutcome.failure("Wrong Answer", outcome.timeSeconds(), "Expected '%s' but got '%s'".formatted(expected, outcome.output()));
            }

            return ExecutionOutcome.success(outcome.timeSeconds());
        } catch (IOException e) {
            log.warn("Execution failed due to IO error", e);
            return ExecutionOutcome.failure("System Error", 0.0, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ExecutionOutcome.failure("System Error", 0.0, "Execution interrupted");
        } finally {
            if (workDir != null) {
                FileSystemUtils.deleteRecursively(workDir.toFile());
            }
        }
    }

    private ExecutionOutcome execute(List<String> command, Path workDir, String input, boolean setWorkingDirectory) throws IOException, InterruptedException {
        Instant start = Instant.now();
        ProcessBuilder builder = new ProcessBuilder(command);
        if (setWorkingDirectory) {
            builder.directory(workDir.toFile());
        }
        builder.redirectErrorStream(false);
        Process process = builder.start();
        try (OutputStream stdin = process.getOutputStream()) {
            stdin.write(input.getBytes(StandardCharsets.UTF_8));
            stdin.flush();
        }
        boolean finished = process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            return ExecutionOutcome.failure("Time Limit Exceeded", Duration.between(start, Instant.now()).toMillis() / 1000.0, "Execution exceeded time limit");
        }
        int exitCode = process.exitValue();
        String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        double elapsed = Duration.between(start, Instant.now()).toMillis() / 1000.0;
        if (exitCode != 0) {
            String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            log.warn("Execution command {} failed with exit code {}: {}", command, exitCode, stderr);
            return ExecutionOutcome.failure("System Error", elapsed, stderr);
        }
        return new ExecutionOutcome(true, "Accepted", elapsed, null, stdout);
    }

    private String normalizeLineEndings(String input) {
        return input.replace("\r\n", "\n").trim();
    }

    private LanguageRuntime resolveRuntime(String language) {
        String normalized = Optional.ofNullable(language).orElse("").toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "python", "py", "python3" -> LanguageRuntime.PYTHON;
            case "javascript", "js", "node" -> LanguageRuntime.NODE;
            default -> LanguageRuntime.UNSUPPORTED;
        };
    }

    private enum LanguageRuntime {
        PYTHON("Main.py", List.of("python3", "Main.py"), List.of("python3", "/workspace/Main.py")),
        NODE("Main.js", List.of("node", "Main.js"), List.of("node", "/workspace/Main.js")),
        UNSUPPORTED("Main.txt", List.of(), List.of());

        private final String sourceFile;
        private final List<String> localCommand;
        private final List<String> containerCommand;

        LanguageRuntime(String sourceFile, List<String> localCommand, List<String> containerCommand) {
            this.sourceFile = sourceFile;
            this.localCommand = localCommand;
            this.containerCommand = containerCommand;
        }

        String sourceFile() {
            return sourceFile;
        }

        List<String> localCommand() {
            if (this == UNSUPPORTED) {
                throw new UnsupportedOperationException("Language is not supported");
            }
            return localCommand;
        }

        List<String> dockerCommand(Path workDir, String image) {
            if (this == UNSUPPORTED) {
                throw new UnsupportedOperationException("Language is not supported");
            }
            List<String> command = new java.util.ArrayList<>();
            command.addAll(List.of(
                    "docker", "run", "--rm", "-i",
                    "--network", "none",
                    "-v", workDir.toAbsolutePath() + ":/workspace",
                    "--memory", "256m",
                    "--cpus", "1",
                    image
            ));
            command.addAll(containerCommand);
            return command;
        }
    }

    public record ExecutionSummary(boolean accepted, double timeSeconds, String verdict, Double score, String message) {
        public static ExecutionSummary accepted(double timeSeconds, String verdict) {
            return new ExecutionSummary(true, timeSeconds, verdict, 1.0, null);
        }

        public static ExecutionSummary accepted(double timeSeconds, String verdict, double score) {
            return new ExecutionSummary(true, timeSeconds, verdict, score, null);
        }

        public static ExecutionSummary failed(String verdict, double timeSeconds, String message) {
            return new ExecutionSummary(false, timeSeconds, verdict, 0.0, message);
        }
    }

    private record ExecutionOutcome(boolean success, String verdict, double timeSeconds, String message, String output) {
        static ExecutionOutcome success(double timeSeconds) {
            return new ExecutionOutcome(true, "Accepted", timeSeconds, null, null);
        }

        static ExecutionOutcome failure(String verdict, double timeSeconds, String message) {
            return new ExecutionOutcome(false, verdict, timeSeconds, message, null);
        }

        public String output() {
            return output == null ? "" : output;
        }
    }
}
