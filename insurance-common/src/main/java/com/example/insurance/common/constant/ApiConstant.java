
package com.example.insurance.common.constant;

public final class ApiConstant {

    private ApiConstant() {}

    public static final String API_VERSION = "/api/v1";
    
    public static final String COMPANIES_PATH = API_VERSION + "/companies";
    public static final String PRODUCTS_PATH = API_VERSION + "/products";
    public static final String VERSIONS_PATH = API_VERSION + "/versions";
    public static final String DOCUMENTS_PATH = API_VERSION + "/documents";
    public static final String PARSE_RESULTS_PATH = API_VERSION + "/parse-results";
    public static final String SYNC_PATH = API_VERSION + "/sync";
    public static final String COMPARE_PATH = API_VERSION + "/compare";
    public static final String AI_PATH = API_VERSION + "/ai";

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_PDF = "application/pdf";
    public static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";
}