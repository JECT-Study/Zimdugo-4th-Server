package com.zimdugo.workflow;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class CiStorageReferenceGuardTest {

    @TempDir
    private Path temporaryDirectory;

    @ParameterizedTest(name = "git grep status {0} produces guard status {1}")
    @CsvSource({
        "0, 1, AWS storage reference remains",
        "1, 0, ''",
        "2, 2, AWS storage scan failed with status 2"
    })
    void distinguishMatchesCleanScanAndScannerFailure(
        int gitGrepStatus,
        int expectedStatus,
        String expectedError
    ) throws Exception {
        installFakeGit();

        ProcessResult result = runGuard(gitGrepStatus);

        assertThat(result.exitCode()).isEqualTo(expectedStatus);
        assertThat(result.standardError()).contains(expectedError);
    }

    private void installFakeGit() throws IOException {
        Path fakeGit = temporaryDirectory.resolve("git");
        Files.writeString(
            fakeGit,
            """
            #!/usr/bin/env bash
            if [ "$#" -ne 11 ] \
              || [ "$1" != "grep" ] \
              || [ "$2" != "-n" ] \
              || [ "$3" != "-E" ] \
              || [ -z "$4" ] \
              || [ "$5" != "--" ] \
              || [ "$6" != "build.gradle.kts" ] \
              || [ "$7" != "src/main" ] \
              || [ "$8" != "src/test" ] \
              || [ "$9" != ".env.example" ] \
              || [ "${10}" != "docker-compose.deploy.yml" ] \
              || [ "${11}" != ".github/workflows/ci-cd.yml" ]; then
              echo "unexpected git invocation" >&2
              exit 64
            fi
            exit "${FAKE_GIT_GREP_STATUS}"
            """,
            StandardCharsets.UTF_8
        );
        Files.setPosixFilePermissions(
            fakeGit,
            PosixFilePermissions.fromString("rwxr-xr-x")
        );
    }

    private ProcessResult runGuard(int gitGrepStatus) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
            "/bin/bash",
            "--noprofile",
            "--norc",
            "-e",
            "-o",
            "pipefail",
            "-c",
            workflowGuard()
        );
        processBuilder.environment().put(
            "PATH",
            temporaryDirectory + ":" + System.getenv("PATH")
        );
        processBuilder.environment().put(
            "FAKE_GIT_GREP_STATUS",
            Integer.toString(gitGrepStatus)
        );
        Process process = processBuilder.start();
        String standardOutput = new String(
            process.getInputStream().readAllBytes(),
            StandardCharsets.UTF_8
        );
        String standardError = new String(
            process.getErrorStream().readAllBytes(),
            StandardCharsets.UTF_8
        );
        int exitCode = process.waitFor();
        return new ProcessResult(exitCode, standardOutput, standardError);
    }

    private String workflowGuard() throws IOException {
        List<String> workflow = Files.readAllLines(
            Path.of(".github/workflows/ci-cd.yml"),
            StandardCharsets.UTF_8
        );
        int stepName = workflow.indexOf("      - name: Reject AWS storage references");
        assertThat(stepName).isNotNegative();
        assertThat(workflow.get(stepName + 1)).isEqualTo("        run: |");
        StringBuilder guard = new StringBuilder();
        for (int index = stepName + 2; index < workflow.size(); index++) {
            String line = workflow.get(index);
            if (!line.startsWith("          ")) {
                break;
            }
            guard.append(line.substring(10)).append('\n');
        }
        return guard.toString();
    }

    private record ProcessResult(
        int exitCode,
        String standardOutput,
        String standardError
    ) {
    }
}
