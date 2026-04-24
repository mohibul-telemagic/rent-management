package com.renteasebd.common;

public record ApiError(String code, String message, String field) {
}
