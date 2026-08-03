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

    @ParameterizedTest(name = "rg status {0} produces guard status {1}")
    @CsvSource({
        "0, 1, AWS storage reference remains",
        "1, 0, ''",
        "2, 2, AWS storage scan failed with status 2"
    })
    void distinguishMatchesCleanScanAndScannerFailure(
        int rgStatus,
        int expectedStatus,
        String expectedError
    ) throws Exception {
        installFakeRipgrep();

        ProcessResult result = runGuard(rgStatus);

        assertThat(result.exitCode()).isEqualTo(expectedStatus);
        assertThat(result.standardError()).contains(expectedError);
    }

    private void installFakeRipgrep() throws IOException {
        Path fakeRipgrep = temporaryDirectory.resolve("rg");
        Files.writeString(
            fakeRipgrep,
            "#!/usr/bin/env bash\nexit \"${FAKE_RG_STATUS}\"\n",
            StandardCharsets.UTF_8
        );
        Files.setPosixFilePermissions(
            fakeRipgrep,
            PosixFilePermissions.fromString("rwxr-xr-x")
        );
    }

    private ProcessResult runGuard(int rgStatus) throws IOException, InterruptedException {
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
        processBuilder.environment().put("FAKE_RG_STATUS", Integer.toString(rgStatus));
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
