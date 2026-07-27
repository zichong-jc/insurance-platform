
package com.example.insurance.api.enums;

import lombok.Getter;

@Getter
public enum ParseStatus {
    
    PENDING("PENDING", "待解析"),
    RUNNING("RUNNING", "解析中"),
    SUCCESS("SUCCESS", "解析成功"),
    FAILED("FAILED", "解析失败"),
    PARTIAL("PARTIAL", "部分解析");

    private final String code;
    private final String description;

    ParseStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ParseStatus fromCode(String code) {
        for (ParseStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return PENDING;
    }
}