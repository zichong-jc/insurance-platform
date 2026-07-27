
package com.example.insurance.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCompareVO {

    private String fieldName;
    private String fieldLabel;
    private List<String> values;
}