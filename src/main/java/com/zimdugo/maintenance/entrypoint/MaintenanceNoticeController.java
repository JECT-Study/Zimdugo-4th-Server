package com.zimdugo.maintenance.entrypoint;

import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.maintenance.application.MaintenanceNoticeService;
import com.zimdugo.maintenance.application.dto.PublicMaintenanceNoticeResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/maintenance-notice")
@RequiredArgsConstructor
public class MaintenanceNoticeController {

    private final MaintenanceNoticeService maintenanceNoticeService;

    @GetMapping
    public ResponseEntity<RestResponse<PublicMaintenanceNoticeResult>> getMaintenanceNotice() {
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, maintenanceNoticeService.getPublicNotice()));
    }
}
