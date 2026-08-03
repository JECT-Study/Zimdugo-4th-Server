package com.zimdugo.common.storage;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "storage.oci")
public record OciObjectStorageProperties(
    @NotBlank
    String region,
    @NotBlank
    String namespace,
    @NotBlank
    String bucket,
    @NotNull
    OciAuthenticationMode authMode,
    @NotBlank
    String configProfile,
    @Min(1)
    @Max(60)
    long uploadExpirationMinutes,
    @Positive
    long maxUploadBytes
) {

    public String objectStorageEndpoint() {
        return "https://objectstorage." + region + ".oraclecloud.com";
    }

    public String publicBaseUrl() {
        return objectStorageEndpoint() + "/n/" + namespace + "/b/" + bucket + "/o";
    }

    public String normalizedConfigProfile() {
        return configProfile.trim();
    }
}
