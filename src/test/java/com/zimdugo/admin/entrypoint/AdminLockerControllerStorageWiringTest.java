package com.zimdugo.admin.entrypoint;

import com.zimdugo.admin.application.AdminLockerImageStorage;
import com.zimdugo.admin.application.AdminNoticeImageFileValidator;
import com.zimdugo.admin.application.OciAdminLockerImageStorage;
import com.zimdugo.admin.i18n.LockerContentI18nAdminService;
import com.zimdugo.admin.locker.AdminLockerService;
import com.zimdugo.common.storage.ImageUploadPolicy;
import com.zimdugo.common.storage.OciImagePathResolver;
import com.zimdugo.common.storage.OciObjectStorageClientProvider;
import com.zimdugo.common.storage.OciObjectStorageProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AdminLockerControllerStorageWiringTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(AdminLockerService.class, () -> mock(AdminLockerService.class))
        .withBean(LockerContentI18nAdminService.class, () -> mock(LockerContentI18nAdminService.class))
        .withBean(OciObjectStorageClientProvider.class, () -> mock(OciObjectStorageClientProvider.class))
        .withBean(OciObjectStorageProperties.class, () -> mock(OciObjectStorageProperties.class))
        .withBean(OciImagePathResolver.class, () -> mock(OciImagePathResolver.class))
        .withBean(ImageUploadPolicy.class, () -> mock(ImageUploadPolicy.class))
        .withBean(AdminNoticeImageFileValidator.class, () -> mock(AdminNoticeImageFileValidator.class))
        .withBean(OciAdminLockerImageStorage.class)
        .withBean(AdminLockerController.class);

    @Test
    void constructLockerControllerWithDedicatedLockerImageStoragePort() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AdminLockerController.class);
            assertThat(context.getBean(AdminLockerImageStorage.class))
                .isInstanceOf(OciAdminLockerImageStorage.class);
        });
    }
}
