package com.kakaopay.backend.spreadapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import lombok.Getter;

@Getter
public enum SpreadExceptionMessages {
    INVALID_REGISTRATION_REQUEST("invalid request.", HttpStatus.BAD_REQUEST),
    NOT_LOADED_HISTORY("history is not loaded.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_CONSISTENCY("SpreadEvent have the invalid consistency.", HttpStatus.CONFLICT),
    FAIL_TO_RESOLVE("Fail to resovle amount.", HttpStatus.INTERNAL_SERVER_ERROR),
    COMPLETED_SPREAD_EVENT("뿌리기 이벤트가 완료되었습니다.", HttpStatus.NOT_MODIFIED),
    NESTED_USER("뿌리기는 한번만 참여 가능합니다.", HttpStatus.NOT_MODIFIED),
    HOST_NOT_ALLOWED("자신이 등록한 뿌리기는 받을 수 없습니다.", HttpStatus.METHOD_NOT_ALLOWED),
    NOT_PARTICIPATED_CHAT_ROOM("참여하지 않은 대화방입니다.", HttpStatus.METHOD_NOT_ALLOWED),
    NOT_EXISTS("뿌리기를 조회 할 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String message;
    private final HttpStatus responseCode;

    SpreadExceptionMessages(String message, HttpStatus responseCode) {
        this.message = message;
        this.responseCode = responseCode;
    }

    public ResponseStatusException newError() {
        return new ResponseStatusException(responseCode, message);
    }
}
