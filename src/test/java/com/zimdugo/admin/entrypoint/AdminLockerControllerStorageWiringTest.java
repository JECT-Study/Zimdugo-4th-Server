package com.zimdugo.admin.entrypoint;

import com.zimdugo.admin.application.AdminLockerImageStorage;
import com.zimdugo.admin.application.AdminNoticeImageFileValidator;
import com.zimdugo.admin.application.S3AdminLockerImageStorage;
import com.zimdugo.admin.i18n.LockerContentI18nAdminService;
import com.zimdugo.admin.locker.AdminLockerService;
import com.zimdugo.common.storage.ImageUploadPolicy;
import com.zimdugo.common.storage.S3ImagePathResolver;
import com.zimdugo.common.storage.S3StorageProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AdminLockerControllerStorageWiringTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(AdminLockerService.class, () -> mock(AdminLockerService.class))
        .withBean(LockerContentI18nAdminService.class, () -> mock(LockerContentI18nAdminService.class))
        .withBean(S3Client.class, () -> mock(S3Client.class))
        .withBean(S3StorageProperties.class, () -> mock(S3StorageProperties.class))
        .withBean(S3ImagePathResolver.class, () -> mock(S3ImagePathResolver.class))
        .withBean(ImageUploadPolicy.class, () -> mock(ImageUploadPolicy.class))
        .withBean(AdminNoticeImageFileValidator.class, () -> mock(AdminNoticeImageFileValidator.class))
        .withBean(S3AdminLockerImageStorage.class)
        .withBean(AdminLockerController.class);

    @Test
    void constructLockerControllerWithDedicatedLockerImageStoragePort() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AdminLockerController.class);
            assertThat(context.getBean(AdminLockerImageStorage.class))
                .isInstanceOf(S3AdminLockerImageStorage.class);
        });
    }
}
