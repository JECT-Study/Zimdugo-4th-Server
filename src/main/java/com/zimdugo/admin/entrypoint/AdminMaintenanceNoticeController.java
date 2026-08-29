package com.zimdugo.admin.entrypoint;

import com.zimdugo.admin.entrypoint.dto.AdminMaintenanceNoticeUpdateRequest;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.maintenance.application.MaintenanceNoticeService;
import com.zimdugo.maintenance.application.dto.AdminMaintenanceNoticeResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/maintenance-notice")
@RequiredArgsConstructor
public class AdminMaintenanceNoticeController {

    private final MaintenanceNoticeService maintenanceNoticeService;

    @GetMapping
    public ResponseEntity<RestResponse<AdminMaintenanceNoticeResult>> getMaintenanceNotice() {
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, maintenanceNoticeService.getAdminNotice()));
    }

    @PutMapping
    public ResponseEntity<RestResponse<AdminMaintenanceNoticeResult>> updateMaintenanceNotice(
        @Valid @RequestBody AdminMaintenanceNoticeUpdateRequest request
    ) {
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, maintenanceNoticeService.update(request.toCommand())));
    }
}
