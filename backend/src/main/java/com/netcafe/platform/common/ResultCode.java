package com.netcafe.platform.common;

public enum ResultCode {
  SUCCESS(0),
  AUTH_FAILED(40101),
  BAD_REQUEST(400),
  UNAUTHORIZED(401),
  FORBIDDEN(403),
  NOT_FOUND(404),
  INTERNAL_ERROR(500);

  private final int code;

  ResultCode(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
