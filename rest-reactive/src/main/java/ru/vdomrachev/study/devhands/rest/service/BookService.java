package ru.vdomrachev.study.devhands.rest.service;

import java.util.HashSet;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vdomrachev.study.devhands.rest.entity.Book;
import ru.vdomrachev.study.devhands.rest.repository.BookRepository;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository repository;
    private final ReactiveHashOperations<String, Long, Book> hashOperations;
    private static final String KEY = "book";

    public Mono<Book> retrieve(long id) {
        return repository.findById(id);
    }

    public Mono<Book> findRandom(int rows) {
        long rand = (long) (Math.random() * rows) + 1;
        return repository.findById(rand);
    }

    public Flux<Book> list() {
        return repository.findAll();
    }

    public Flux<Book> getRandom(int rows, int limit) {
        Set<Long> ids = generateUniqueRandomNumbersFromDiapason(limit, 1, rows);
        return repository.findAllById(ids);
    }

    public Flux<Book> getRandomCached(int rows, int limit) {
        Set<Long> ids = generateUniqueRandomNumbersFromDiapason(limit, 1, rows);
        return Flux.fromIterable(ids).flatMap(this::retrieveCached);
    }

    private Set<Long> generateUniqueRandomNumbersFromDiapason(int limit, int start, int end) {
        if (limit > end - start) {
            throw new IllegalArgumentException("Limit is bigger than diapason");
        } else {
            Set<Long> ids = new HashSet<>();
            while (ids.size() < limit) {
                ids.add((long) (Math.random() * (end - start) + start));
            }
            return ids;
        }
    }

    private Mono<Book> updateRedisCache(Book book) {
        return hashOperations.put(KEY, book.getId(), book)
                .thenReturn(book);
    }

    public Mono<Book> save(Book book) {
        return save(book).flatMap(this::updateRedisCache);
    }

    public Mono<Book> retrieveCached(Long id) {
        return hashOperations.get(KEY, id)
                .switchIfEmpty(retrieve(id))
                .flatMap(this::updateRedisCache);
    }
}