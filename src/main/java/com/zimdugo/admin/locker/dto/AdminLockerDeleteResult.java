package com.zimdugo.admin.locker.dto;

public record AdminLockerDeleteResult(
    boolean hardDeleted,
    String message
) {
    public static AdminLockerDeleteResult deletedResult() {
        return new AdminLockerDeleteResult(true, "보관함을 삭제했습니다.");
    }

    public static AdminLockerDeleteResult deactivatedWithIssueReportsResult() {
        return new AdminLockerDeleteResult(
            false,
            "신고 이력이 있는 보관함은 삭제하지 않고 비공개 처리했습니다."
        );
    }
}
