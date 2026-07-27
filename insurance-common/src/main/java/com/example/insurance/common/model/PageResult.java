
package com.example.insurance.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResult<T> {

    private Integer code;
    private String message;
    private List<T> data;
    private Long total;
    private Integer page;
    private Integer size;
    private String timestamp;

    public static <T> PageResult<T> success(List<T> data, Long total, Integer page, Integer size) {
        return PageResult.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .total(total)
                .page(page)
                .size(size)
                .timestamp(Instant.now().toString())
                .build();
    }

    public static <T> PageResult<T> empty(Integer page, Integer size) {
        return PageResult.<T>builder()
                .code(200)
                .message("success")
                .data(List.of())
                .total(0L)
                .page(page)
                .size(size)
                .timestamp(Instant.now().toString())
                .build();
    }
}