package ru.vdomrachev.study.devhands.rest.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vdomrachev.study.devhands.rest.entity.Book;

public interface BookRepository extends ReactiveCrudRepository<Book, Long> {

    @Query("""
        SELECT * FROM book
        ORDER BY RANDOM()
        LIMIT 1
        """)
    Mono<Book> findRandomBook();

    @Query("""
            SELECT * FROM book
            ORDER BY RANDOM()
            LIMIT :count
        """)
    Flux<Book> getRandomBooks(@Param("count") long count);

}
