package com.qaq.gateway.enums;

import lombok.Getter;

@Getter
public enum GatewayErrorEnum {

    EXPIRED_TOKEN(10001, 401, "Gateway: expired token"),
    INVALID_TOKEN(10002, 401, "Gateway: invalid token"),
    MISSING_EMAIL(10003, 401, "Gateway: missing user email in token"),
    MISSING_AUTH(10004, 401, "Gateway: no authorization info"),
    INVALID_HEADER(10005, 401, "Gateway: invalid authorization header"),
    MULTIPLE_TOKEN(10006, 401, "Gateway: multipe value jwt in cookies"),
    NOT_FOUND(10007, 404, "Gateway: invalid url"),
    INTERNAL_ERROR(10008, 500, "Gateway: gateway server error"),
    SERIVICE_TIMEOUT(10009, 504, "Gateway: service server response timeout"),
    SERIVICE_NOT_AVAILABLE(10010, 503, "Gateway: service server not available"),
    NO_PERMISSION(10011, 401, "Gateway: has no permission to access");

    private int errcode;
    private int httpStatus;
    private String message;

    private GatewayErrorEnum(int errorCode, int httpStatus, String errorMsg) {
        this.errcode = errorCode;
        this.httpStatus = httpStatus;
        this.message = errorMsg;
    }

}
