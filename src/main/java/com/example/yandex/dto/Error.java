package com.example.yandex.dto;

import lombok.Data;

@Data
public class Error {
    private int code;

    private String message;

    public Error(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
