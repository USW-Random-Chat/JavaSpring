package com.USWRandomChat.backend.global.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiResponse {
    private String message;
    private Object data;

    public ApiResponse(String message) {
        this.message = message;
        this.data = null; // 데이터가 없는 경우
    }

    public ApiResponse(String message, Object data) {
        this.message = message;
        this.data = data; // 데이터를 포함하는 경우
    }
}
