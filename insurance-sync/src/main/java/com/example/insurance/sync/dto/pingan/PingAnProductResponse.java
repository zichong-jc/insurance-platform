package com.example.insurance.sync.dto.pingan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 平安产品列表接口响应
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PingAnProductResponse {

    @JsonProperty("resultCode")
    private String resultCode;

    @JsonProperty("resultMsg")
    private String resultMsg;

    @JsonProperty("data")
    private List<CategoryData> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryData {
        @JsonProperty("categoryCode")
        private String categoryCode;

        @JsonProperty("categoryName")
        private String categoryName;

        @JsonProperty("categoryType")
        private String categoryType;

        @JsonProperty("parentCategoryCode")
        private String parentCategoryCode;

        @JsonProperty("parentCategoryName")
        private String parentCategoryName;

        @JsonProperty("categoryProducts")
        private List<ProductData> categoryProducts;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductData {
        @JsonProperty("productId")
        private String productId;

        @JsonProperty("productCode")
        private String productCode;

        @JsonProperty("productName")
        private String productName;

        @JsonProperty("productCategory")
        private String productCategory;

        @JsonProperty("categoryCode")
        private String categoryCode;

        @JsonProperty("categoryName")
        private String categoryName;

        @JsonProperty("productFeatures")
        private String productFeatures;

        @JsonProperty("productLabels")
        private String productLabels;

        @JsonProperty("productArea")
        private String productArea;

        @JsonProperty("productPic")
        private String productPic;

        @JsonProperty("productUrl")
        private String productUrl;

        @JsonProperty("productPrice")
        private Double productPrice;

        @JsonProperty("priceUnit")
        private String priceUnit;

        @JsonProperty("productDesc")
        private String productDesc;

        @JsonProperty("homeProductFlag")
        private String homeProductFlag;

        @JsonProperty("hotProductFlag")
        private String hotProductFlag;

        @JsonProperty("newProductFlag")
        private String newProductFlag;

        @JsonProperty("productOrder")
        private Integer productOrder;
    }
}