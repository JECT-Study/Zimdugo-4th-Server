package com.zimdugo.admin.ui;

import com.zimdugo.admin.i18n.LockerContentI18nAdminService;
import com.zimdugo.admin.ui.dto.AdminLockerI18nRequest;
import com.zimdugo.admin.ui.dto.AdminLockerI18nResponse;
import com.zimdugo.admin.ui.dto.AdminPlaceI18nRequest;
import com.zimdugo.admin.ui.dto.AdminPlaceI18nResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/i18n")
@RequiredArgsConstructor
public class AdminLockerContentI18nController {

    private final LockerContentI18nAdminService service;

    @GetMapping("/places/{id}")
    public ResponseEntity<AdminPlaceI18nResponse> getPlace(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPlace(id));
    }

    @PutMapping("/places/{id}")
    public ResponseEntity<AdminPlaceI18nResponse> replacePlace(
        @PathVariable Long id,
        @Valid @RequestBody AdminPlaceI18nRequest request
    ) {
        return ResponseEntity.ok(service.replacePlace(id, request));
    }

    @GetMapping("/lockers/{id}")
    public ResponseEntity<AdminLockerI18nResponse> getLocker(@PathVariable Long id) {
        return ResponseEntity.ok(service.getLocker(id));
    }

    @PutMapping("/lockers/{id}")
    public ResponseEntity<AdminLockerI18nResponse> replaceLocker(
        @PathVariable Long id,
        @Valid @RequestBody AdminLockerI18nRequest request
    ) {
        return ResponseEntity.ok(service.replaceLocker(id, request));
    }
}
