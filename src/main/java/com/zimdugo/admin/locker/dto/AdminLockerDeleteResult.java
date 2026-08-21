package com.zimdugo.admin.locker.dto;

public record AdminLockerDeleteResult(
    boolean softDeleted,
    String message
) {
    public static AdminLockerDeleteResult softDeletedResult() {
        return new AdminLockerDeleteResult(
            true,
            "보관함을 삭제 처리했습니다. 신고 이력 보존을 위해 데이터는 soft delete로 유지됩니다."
        );
    }
}
