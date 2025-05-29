package ru.vdomrachev.study.devhands.rest.service;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vdomrachev.study.devhands.rest.entity.Book;
import ru.vdomrachev.study.devhands.rest.repository.BookRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository repository;
    private final ReactiveHashOperations<String, Long, Book> hashOperations;
    private static final String KEY = "book";

    public Mono<Book> retrieve(long id) {
        return repository.findById(id);
    }

    public Mono<Book> findRandom() {
        return repository.findRandomBook()
                .timeout(Duration.ofSeconds(5))  // Prevent hanging connections
                .retry(2)                        // Retry on transient failures
                .onErrorResume(ex -> {
                    log.error("Error fetching random books", ex);
                    return Mono.empty();         // Graceful degradation
                });
    }

    public Flux<Book> list() {
        return repository.findAll();
    }

    public Flux<Book> getRandomBooks(long limit) {
        return repository.getRandomBooks(limit)
                .timeout(Duration.ofSeconds(5))  // Prevent hanging connections
                .retry(2)                        // Retry on transient failures
                .onErrorResume(ex -> {
                    log.error("Error fetching random books", ex);
                    return Flux.empty();         // Graceful degradation
                });
    }

    public Flux<Book> getRandomBooks(int rows, int limit) {
        return generateUniqueRandomIds(limit, 1, rows)
                .collectList()
                .flatMapMany(repository::findAllById);
    }

    public Flux<Book> getRandomCached(int rows, int limit) {
        return generateUniqueRandomIds(limit, 1, rows)
                .flatMap(this::retrieveCached);
    }

    public Flux<Long> generateUniqueRandomIds(int count, long minId, long maxId) {
        if (count <= 0 || minId >= maxId || (maxId - minId + 1) < count) {
            return Flux.error(new IllegalArgumentException("Cannot generate unique IDs with given range"));
        }

        return Flux.generate(
                () -> ThreadLocalRandom.current().longs(minId, maxId + 1).distinct().limit(count).iterator(),
                (iterator, sink) -> {
                    if (iterator.hasNext()) {
                        sink.next(iterator.next());
                    } else {
                        sink.complete();
                    }
                    return iterator;
                }
        );
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