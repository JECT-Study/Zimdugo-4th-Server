package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.domain.issue.LockerIssueReportStatus;
import com.zimdugo.locker.domain.issue.LockerIssueReportType;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "locker_issue_reports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LockerIssueReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "locker_id", nullable = false)
    private Long lockerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private LockerIssueReportType reportType;

    @Column(name = "detail", length = 1000)
    private String detail;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", nullable = false, length = 30)
    private LockerIssueReportStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "review_note", length = 1000)
    private String reviewNote;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Builder
    private LockerIssueReportEntity(
        Long lockerId,
        LockerIssueReportType reportType,
        String detail,
        LockerIssueReportStatus status
    ) {
        this.lockerId = lockerId;
        this.reportType = reportType;
        this.detail = detail;
        this.status = status;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void resolve(String reviewer, String reviewNote) {
        validatePending();
        this.status = LockerIssueReportStatus.RESOLVED;
        recordReview(reviewer, reviewNote);
    }

    public void reject(String reviewer, String reviewNote) {
        validatePending();
        this.status = LockerIssueReportStatus.REJECTED;
        recordReview(reviewer, reviewNote);
    }

    private void validatePending() {
        if (status != LockerIssueReportStatus.PENDING) {
            throw new BusinessException(ErrorCode.LOCKER_ISSUE_REPORT_ALREADY_REVIEWED);
        }
    }

    private void recordReview(String reviewer, String reviewNote) {
        this.reviewedBy = reviewer;
        this.reviewNote = reviewNote;
        this.reviewedAt = LocalDateTime.now();
    }
}
