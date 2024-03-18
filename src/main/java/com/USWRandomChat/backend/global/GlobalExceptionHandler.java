package com.USWRandomChat.backend.global;

import com.USWRandomChat.backend.global.response.ErrorResponse;
import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        HttpStatus status = INTERNAL_SERVER_ERROR;
        String code = "NO_CATCH_ERROR";
        String className = e.getClass().getName();
        String message = e.getMessage();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .exception(className.substring(className.lastIndexOf(".") + 1))
                .code(code)
                .message(message)
                .status(status.value())
                .error(status.getReasonPhrase())
                .build();

        log.error("code : {}, message : {}", errorResponse.getCode(), errorResponse.getMessage());

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(value = {BaseException.class})
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
        String className = e.getClass().getName();
        ExceptionType exceptionType = e.getExceptionType();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .exception(className.substring(className.lastIndexOf(".") + 1))
                .code(exceptionType.getCode())
                .message(exceptionType.getMessage())
                .status(exceptionType.getStatus().value())
                .error(exceptionType.getStatus().getReasonPhrase())
                .build();

        log.error("code : {}, message : {}", errorResponse.getCode(), errorResponse.getMessage());

        return new ResponseEntity<>(errorResponse, exceptionType.getStatus());
    }

}