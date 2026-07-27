
package com.example.insurance.api.enums;

import lombok.Getter;

@Getter
public enum SyncStatus {
    
    PENDING("PENDING", "待同步"),
    RUNNING("RUNNING", "同步中"),
    SUCCESS("SUCCESS", "同步成功"),
    FAILED("FAILED", "同步失败"),
    SKIPPED("SKIPPED", "已跳过");

    private final String code;
    private final String description;

    SyncStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static SyncStatus fromCode(String code) {
        for (SyncStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return PENDING;
    }
}