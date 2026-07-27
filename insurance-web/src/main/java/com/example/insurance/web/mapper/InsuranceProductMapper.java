
package com.example.insurance.web.mapper;

import com.example.insurance.api.dto.request.InsuranceProductRequest;
import com.example.insurance.api.dto.response.InsuranceProductResponse;
import com.example.insurance.web.entity.InsuranceProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InsuranceProductMapper {

    @Mapping(target = "typeDescription", expression = "java(product.getType() != null ? product.getType().getDescription() : null)")
    @Mapping(target = "companyName", ignore = true)
    @Mapping(target = "versionCount", ignore = true)
    InsuranceProductResponse toResponse(InsuranceProduct product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "type", expression = "java(com.example.insurance.api.enums.ProductType.fromCode(request.getType()))")
    InsuranceProduct toEntity(InsuranceProductRequest request);
}