package com.zimdugo.admin.entrypoint.dto;

import com.zimdugo.admin.report.dto.AdminLockerReportApprovalCommand;
import com.zimdugo.admin.report.dto.AdminLockerReportReviewPageResult;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminLockerReportApprovalForm {

    private Long existingPlaceId;

    private String kakaoPlaceId;

    @Size(max = 120)
    private String placeName;

    @NotBlank
    @Size(max = 100)
    private String lockerName;

    @NotBlank
    @Size(max = 255)
    private String roadAddress;

    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double latitude;

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double longitude;

    public static AdminLockerReportApprovalForm from(AdminLockerReportReviewPageResult.Report report) {
        AdminLockerReportApprovalForm form = new AdminLockerReportApprovalForm();
        form.setLockerName(report.name());
        form.setRoadAddress(report.roadAddress());
        form.setLatitude(report.latitude());
        form.setLongitude(report.longitude());
        return form;
    }

    public AdminLockerReportApprovalCommand toCommand() {
        return new AdminLockerReportApprovalCommand(
            existingPlaceId,
            kakaoPlaceId,
            placeName,
            lockerName,
            roadAddress,
            latitude,
            longitude
        );
    }
}
