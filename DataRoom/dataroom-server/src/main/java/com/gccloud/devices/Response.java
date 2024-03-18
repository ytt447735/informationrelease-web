package com.gccloud.devices;

public class Response {
    private int code;
    private String msg;
    private Object data;

    public Response() {}

    public Response(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // getters and setters

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    // Static method for creating response instances
    public static Response ok(Object data) {
        return new Response(200, "success", data);
    }

    public static Response error(String message) {
        return new Response(500, message, null);
    }
}

