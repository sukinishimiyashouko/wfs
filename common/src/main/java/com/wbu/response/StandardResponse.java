package com.wbu.response;

import com.wbu.interfaces.IResponse;

/**
 * @auther 11852
 * @create 2023/7/31
 */
public enum StandardResponse implements IResponse {
    OK(200,"success"),
    BAD_REQUEST(400,"Bad Request"),
    FORBIDDEN(403,"Access Denied"),
    NOT_FOUND(404,"Not Found"),
    ERROR(500,"Business Error");
    ;
    private final int code;
    private final String message;

    StandardResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
