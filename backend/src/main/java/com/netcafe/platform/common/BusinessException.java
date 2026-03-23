package com.netcafe.platform.common;

public class BusinessException extends RuntimeException {
  private final ResultCode code;

  public BusinessException(ResultCode code, String message) {
    super(message);
    this.code = code;
  }

  public ResultCode getCode() {
    return code;
  }
}
