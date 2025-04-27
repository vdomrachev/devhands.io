package ru.vdomrachev.study.devhands.rest.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.vdomrachev.study.devhands.rest.entity.Book;

public interface BookRepository extends ReactiveCrudRepository<Book, Long> {
}
