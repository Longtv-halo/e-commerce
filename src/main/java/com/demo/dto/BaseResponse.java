package com.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private boolean success;
    private T results;
    private ErrorDetail error;
    private ResultInfo resultInfo;

    public static <T> BaseResponse<T> ok(T results) {
        return BaseResponse.<T>builder()
                .success(true)
                .results(results)
                .build();
    }

    public static <T> BaseResponse<T> ok(T results, ResultInfo resultInfo) {
        return BaseResponse.<T>builder()
                .success(true)
                .results(results)
                .resultInfo(resultInfo)
                .build();
    }

    public static <T> BaseResponse<T> fail(String code, String message) {
        return BaseResponse.<T>builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code(code)
                        .message(message)
                        .build())
                .build();
    }

    public static <T> BaseResponse<T> created(T results) {
        return BaseResponse.<T>builder()
                .success(true)
                .results(results)
                .build();
    }
}
