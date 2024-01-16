package org.gzu.adminbackend.model.vo;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

/**
 * @description 统一REST响应
 * @classname RestBean
 * @date 1/16/2024 10:16 PM
 * @created by LIONS7
 */
public record RestBean<T>(int code, T data, String message) {

    public static <T> RestBean<T> success(T data, String message) {
        return new RestBean<>(200, data, message);
    }

    public static <T> RestBean<T> failure(int code, String message) {
        return new RestBean<>(code, null, message);
    }

    public static <T> RestBean<T> unAuthorized(String message) {
        return new RestBean<>(401, null, message);
    }

    public static <T> RestBean<T> forbidden(String message) {
        return new RestBean<>(403, null, message);
    }

    public String toJsonStr() {
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }
}
