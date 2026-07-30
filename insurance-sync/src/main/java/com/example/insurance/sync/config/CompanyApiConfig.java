package com.example.insurance.sync.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 保险公司 API 配置
 * 各保险公司接口统一配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "sync.company")
public class CompanyApiConfig {

    /**
     * 保险公司配置映射
     * key: 公司代码 (如: PING_AN, PICC, PACIFIC)
     * value: 公司API配置
     */
    private Map<String, CompanyConfig> configs;

    @Data
    public static class CompanyConfig {
        /**
         * 公司名称
         */
        private String name;

        /**
         * 产品列表接口URL
         */
        private String productListUrl;

        /**
         * 产品详情接口URL
         */
        private String productDetailUrl;

        /**
         * 条款下载接口URL
         */
        private String clauseDownloadUrl;

        /**
         * 请求头配置
         */
        private Map<String, String> headers;

        /**
         * 请求参数配置
         */
        private Map<String, String> params;

        /**
         * 接口认证配置
         */
        private AuthConfig auth;
    }

    @Data
    public static class AuthConfig {
        /**
         * 认证类型: NONE, API_KEY, OAUTH2
         */
        private String type;

        /**
         * API Key
         */
        private String apiKey;

        /**
         * App Secret
         */
        private String appSecret;

        /**
         * Token URL (OAuth2)
         */
        private String tokenUrl;
    }
}