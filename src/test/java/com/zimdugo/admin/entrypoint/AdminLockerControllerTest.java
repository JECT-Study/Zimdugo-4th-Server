package com.zimdugo.admin.entrypoint;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zimdugo.admin.application.AdminNoticeImageStorage;
import com.zimdugo.admin.i18n.LockerContentI18nAdminService;
import com.zimdugo.admin.locker.AdminLockerService;
import com.zimdugo.admin.locker.dto.AdminLockerDetailResult;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

class AdminLockerControllerTest {

    @Test
    void deletesStoredImageOnlyAfterLockerDeletionSucceeds() {
        AdminLockerService lockerService = mock(AdminLockerService.class);
        AdminNoticeImageStorage imageStorage = mock(AdminNoticeImageStorage.class);
        AdminLockerDetailResult locker = mock(AdminLockerDetailResult.class);
        when(lockerService.getLocker(1L)).thenReturn(locker);
        when(locker.imageUrl()).thenReturn("https://cdn.example.com/locker.jpg");
        AdminLockerController controller = new AdminLockerController(
            lockerService,
            mock(LockerContentI18nAdminService.class),
            imageStorage
        );

        controller.delete(1L, mock(RedirectAttributes.class));

        InOrder order = inOrder(lockerService, imageStorage);
        order.verify(lockerService).deleteLocker(1L);
        order.verify(imageStorage).deleteAll(List.of("https://cdn.example.com/locker.jpg"));
    }
}
