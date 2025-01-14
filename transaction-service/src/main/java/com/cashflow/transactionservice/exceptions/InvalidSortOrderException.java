package com.cashflow.transactionservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidSortOrderException extends RuntimeException {

    public InvalidSortOrderException(String message) {
        super(message);
    }

}
