package com.example.bookcatalog.book;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/** Rendered by Spring MVC as an RFC 9457 problem+json response with status 404. */
public class BookNotFoundException extends ErrorResponseException {

    public BookNotFoundException(long id) {
        super(HttpStatus.NOT_FOUND,
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Book with id " + id + " not found"),
                null);
    }
}
