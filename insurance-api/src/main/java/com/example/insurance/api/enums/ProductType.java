
package com.example.insurance.api.enums;

import lombok.Getter;

@Getter
public enum ProductType {

    LIFE("LI", "寿险"),
    CRITICAL_ILLNESS("CI", "重疾险"),
    ACCIDENT("AC", "意外险"),
    TERM_LIFE("TL", "定期寿险"),
    WHOLE_LIFE("WL", "终身寿险"),
    HEALTH("HE", "医疗险"),
    ANNUITY("AN", "年金险"),
    AUTO("AU", "车险"),
    CAR("CA", "车险"),
    PROPERTY("PR", "财产险"),
    HOME("HO", "家财险"),
    TRAVEL("TR", "旅游险"),
    OTHER("OT", "其他");

    private final String code;
    private final String description;

    ProductType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ProductType fromCode(String code) {
        for (ProductType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return OTHER;
    }
}