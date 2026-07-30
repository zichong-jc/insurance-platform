
package com.example.insurance.api.enums;

import lombok.Getter;

@Getter
public enum CompanyType {

    PING_AN("PA", "平安保险"),
    PACIFIC("PC", "太平洋保险"),
    PICC("PI", "中国人保"),
    SUNSHINE("SS", "阳光保险"),
    TAIPING("TP", "太平保险"),
    NEW_CHINA("NC", "新华保险"),
    TAIKANG("TK", "泰康保险"),
    ALLIANZ("AZ", "安联保险"),
    PROPERTY("PR", "财产保险"),
    OTHER("OT", "其他");

    private final String code;
    private final String description;

    CompanyType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static CompanyType fromCode(String code) {
        for (CompanyType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return OTHER;
    }
}