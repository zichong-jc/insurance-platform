
package com.example.insurance.sync.strategy;

import com.example.insurance.api.enums.CompanyType;
import com.example.insurance.sync.entity.SyncLog;

import java.util.List;

public interface InsuranceSync {

    CompanyType getCompanyType();

    String getCompanyCode();

    SyncLog syncAllProducts();

    SyncLog syncProduct(Long productId);

    List<SyncLog> syncProducts(List<Long> productIds);

    boolean isSupported();

    String getCompanyName();
}