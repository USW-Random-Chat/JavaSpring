package com.USWRandomChat.backend.exception.errortype;

import com.USWRandomChat.backend.exception.ExceptionType;

public class AccountException extends BaseException {

    public AccountException(ExceptionType exceptionType) {
        super(exceptionType);
    }
}