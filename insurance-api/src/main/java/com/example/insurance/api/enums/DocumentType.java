
package com.example.insurance.api.enums;

import lombok.Getter;

@Getter
public enum DocumentType {
    
    INSURANCE_POLICY("IP", "保险条款"),
    PRODUCT_BROCHURE("PB", "产品说明书"),
    CLAIM_GUIDE("CG", "理赔指南"),
    DRUG_MANUAL("DM", "药品说明书"),
    CHIP_DATASHEET("CD", "芯片规格书"),
    LAW_REGULATION("LR", "法律法规"),
    MEDICAL_DEVICE("MD", "医疗器械说明书"),
    OTHER("OT", "其他");

    private final String code;
    private final String description;

    DocumentType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static DocumentType fromCode(String code) {
        for (DocumentType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return OTHER;
    }
}