
package com.example.insurance.web.mapper;

import com.example.insurance.api.dto.request.InsuranceCompanyRequest;
import com.example.insurance.api.dto.response.InsuranceCompanyResponse;
import com.example.insurance.web.entity.InsuranceCompany;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InsuranceCompanyMapper {

    @Mapping(target = "typeDescription", expression = "java(company.getType() != null ? company.getType().getDescription() : null)")
    InsuranceCompanyResponse toResponse(InsuranceCompany company);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "type", expression = "java(com.example.insurance.api.enums.CompanyType.fromCode(request.getType()))")
    InsuranceCompany toEntity(InsuranceCompanyRequest request);
}