package com.example.bookcatalog.book;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

/** Deliberately simple in-memory store so the demo has no database dependency. */
@Repository
public class BookRepository {

    private final Map<Long, Book> books = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public List<Book> findAll() {
        return books.values().stream().sorted(Comparator.comparingLong(Book::id)).toList();
    }

    public Optional<Book> findById(long id) {
        return Optional.ofNullable(books.get(id));
    }

    public Book create(BookRequest request) {
        Book book = Book.from(sequence.incrementAndGet(), request);
        books.put(book.id(), book);
        return book;
    }

    public Optional<Book> update(long id, BookRequest request) {
        return Optional.ofNullable(books.computeIfPresent(id, (key, existing) -> Book.from(key, request)));
    }

    public boolean delete(long id) {
        return books.remove(id) != null;
    }
}
