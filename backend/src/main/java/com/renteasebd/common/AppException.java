package com.renteasebd.common;

public class AppException extends RuntimeException {

    private final String code;
    private final String field;

    public AppException(String code, String message, String field) {
        super(message);
        this.code = code;
        this.field = field;
    }

    public String getCode() {
        return code;
    }

    public String getField() {
        return field;
    }
}
