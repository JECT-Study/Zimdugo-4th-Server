package com.zimdugo.admin.domain;

import lombok.Getter;

@Getter
public enum DocumentType {
    NOTICE("서비스 공지사항"),
    TERMS("이용 약관"),
    PRIVACY("개인정보처리방침");

    private final String description;

    DocumentType(String description) {
        this.description = description;
    }
}
