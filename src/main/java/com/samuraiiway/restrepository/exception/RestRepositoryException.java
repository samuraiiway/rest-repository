package com.samuraiiway.restrepository.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class RestRepositoryException extends RuntimeException {
    private HttpStatus httpStatus;

    private String code;

    private String message;

    public RestRepositoryException(HttpStatus httpStatus, String code) {
        super(code);
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public RestRepositoryException(HttpStatus httpStatus, String code, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
