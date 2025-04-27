package ru.vdomrachev.study.devhands.rest.service;

import java.util.HashSet;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vdomrachev.study.devhands.rest.entity.Book;
import ru.vdomrachev.study.devhands.rest.repository.BookRepository;

@Service
@RequiredArgsConstructor
public class BookService {
     
    private final BookRepository repository;
    @Lazy @Autowired
    private BookService service;

    public Mono<Book> retrieve(long id) {
        return repository.findById(id);
    }

    public Mono<Book> findRandom(int rows) {
        long rand = (long)(Math.random() * rows) + 1;
        return repository.findById(rand);
    }

    public Flux<Book> list() {
        return repository.findAll();
    }

    public Flux<Book> getRandom(int rows, int limit) {
        Set<Long> ids = generateUniqueRandomNyumbersFromDiapason(limit, 0, rows);
        return repository.findAllById(ids);
    }

    private Set<Long> generateUniqueRandomNyumbersFromDiapason(int limit,  int start, int end) {
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
}