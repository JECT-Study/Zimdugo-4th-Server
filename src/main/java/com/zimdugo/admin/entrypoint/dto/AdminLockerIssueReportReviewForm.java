package com.zimdugo.admin.entrypoint.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminLockerIssueReportReviewForm {

    @Size(max = 1000)
    private String reviewMemo;
}
