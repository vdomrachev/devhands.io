package ru.vdomrachev.study.devhands.rest.service;

import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import ru.vdomrachev.study.devhands.rest.entity.Book;
import ru.vdomrachev.study.devhands.rest.repository.BookRepository;

@Service
@RequiredArgsConstructor
public class BookService {
     
    private final BookRepository repository;
    @Lazy @Autowired
    private BookService service;
    
    public List<Book> getRandomBooks(Integer rows, Integer limit) {
        return repository.findRandomBooks(rows, limit);
    }

    public List<Book> getRandomBooksCached(Integer rows, Integer limit) {
        Set<Long> ids = generateUniqueRandomNumbersFromDiapason(limit, 1, rows);
        List<Book> books = new ArrayList<>();
        for (Long id : ids) {
            Optional<Book> bookOptional = service.findByIdCached(id);
            bookOptional.ifPresent(books::add);
        }
        return books;
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

    public Page<Book> getBookPagination(Integer pageNumber, Integer pageSize, String sort) {
        Pageable pageable;
        if (sort != null) {
            // with sorting
            pageable = PageRequest.of(pageNumber, pageSize, Sort.Direction.ASC, sort);
        } else {
            // without sorting
            pageable = PageRequest.of(pageNumber, pageSize);
        }
        return repository.findAll(pageable);
    }

    public Book findRandom(Integer rows) {
        long rand = (long)(Math.random() * rows) + 1;
        return repository.findById(rand).orElse(null);
    }

    public Book findRandomCached(Integer rows) {
    	long rand = (long)(Math.random() * rows) + 1;
        return service.findByIdCached(rand).orElse(null);
    }

    @Cacheable(value = "books", key = "#id")
    public Optional<Book> findByIdCached(Long id) {
        return repository.findById(id);
    }

    public Optional<Book> findById(Long id) {
        return repository.findById(id);
    }
}