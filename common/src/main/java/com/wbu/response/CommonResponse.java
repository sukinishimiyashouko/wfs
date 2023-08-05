package com.wbu.response;

import com.wbu.interfaces.IResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * @auther 11852
 * @create 2023/7/31
 */
@Data
@AllArgsConstructor
public class CommonResponse<T> implements IResponse {
    private final int code;
    private final String message;
    private final T data;

    public static <T> CommonResponse<T> success(){
        return success(null);
    }
    public static <T> CommonResponse<T> success(T data){
        return new CommonResponse<>(200,"success",data);
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
