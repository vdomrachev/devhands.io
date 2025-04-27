package ru.vdomrachev.study.devhands.rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import ru.vdomrachev.study.devhands.rest.entity.Book;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    @NativeQuery("""
        SELECT *
        FROM  (
            SELECT DISTINCT 1 + trunc(random() * ?1)::integer AS id
            FROM generate_series(1, ?2 * 1.1) g
        ) r
        JOIN   book USING (id)
        LIMIT  ?2;
    """)
    List<Book> findRandomBooks(Integer rows, Integer limit);
}
