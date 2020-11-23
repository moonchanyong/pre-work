package com.kakaopay.backend.spreadapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import lombok.Getter;

// 뿌리기 도메인과 별개로 공통 Exception.
@Getter
public enum CommonExceptionMessages {
    NOT_EXISTS_USER_ID("유저 아이디가 없습니다.", HttpStatus.UNAUTHORIZED);

    private final String message;
    private final HttpStatus responseCode;

    CommonExceptionMessages(String message, HttpStatus responseCode) {
        this.message = message;
        this.responseCode = responseCode;
    }

    public ResponseStatusException newError() {
        return new ResponseStatusException(responseCode, message);
    }
}
