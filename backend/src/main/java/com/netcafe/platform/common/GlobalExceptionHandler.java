package com.netcafe.platform.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BusinessException.class)
  public ApiResponse<Void> handleBusinessException(BusinessException exception) {
    return ApiResponse.fail(exception.getCode(), exception.getMessage());
  }
}
