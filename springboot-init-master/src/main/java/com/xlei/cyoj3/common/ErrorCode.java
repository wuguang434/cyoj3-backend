package com.xlei.cyoj3.common;

/**
 * 自定义错误码
 * <p>
 * # @author <a href="https://github.com/wuguang434">Coding boy:xlei</a>
 */
public enum ErrorCode {

    SUCCESS(0, "ok"),
    ERROR_NULL(40001,"请求数据为空"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    TOO_MANY_REQUEST(42500,"请求过于频繁"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),
    API_REQUEST_ERROR(50010,"接口调用错误");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
