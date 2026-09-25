package com.example.bookcatalog.book;

public record Book(long id, String title, String author, String isbn, int publishedYear) {

    static Book from(long id, BookRequest request) {
        return new Book(id, request.title(), request.author(), request.isbn(), request.publishedYear());
    }
}
