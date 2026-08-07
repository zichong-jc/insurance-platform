package com.example.insurance.api.enums;

import lombok.Getter;

@Getter
public enum VersionStatus {

    DRAFT("DRAFT", "草稿"),
    ACTIVE("ACTIVE", "生效"),
    SUPERSEDED("SUPERSEDED", "已替代"),
    ARCHIVED("ARCHIVED", "已归档");

    private final String code;
    private final String description;

    VersionStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static VersionStatus fromCode(String code) {
        for (VersionStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return DRAFT;
    }
}