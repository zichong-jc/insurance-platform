
package com.example.insurance.web.mapper;

import com.example.insurance.api.dto.response.DocumentFileResponse;
import com.example.insurance.api.dto.response.DocumentParseResultResponse;
import com.example.insurance.web.entity.DocumentFile;
import com.example.insurance.web.entity.DocumentParseResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(target = "documentTypeDescription", expression = "java(doc.getDocumentType() != null ? doc.getDocumentType().getDescription() : null)")
    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "versionNumber", ignore = true)
    DocumentFileResponse toDocumentResponse(DocumentFile doc);

    @Mapping(target = "fileName", ignore = true)
    @Mapping(target = "productName", ignore = true)
    DocumentParseResultResponse toParseResultResponse(DocumentParseResult result);
}