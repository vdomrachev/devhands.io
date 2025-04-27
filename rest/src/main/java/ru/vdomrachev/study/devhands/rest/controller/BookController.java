package ru.vdomrachev.study.devhands.rest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vdomrachev.study.devhands.rest.dto.BookDto;
import ru.vdomrachev.study.devhands.rest.entity.Book;
import ru.vdomrachev.study.devhands.rest.mapper.BookMapper;
import ru.vdomrachev.study.devhands.rest.service.BookService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService service;

    private final BookMapper mapper;

    @GetMapping("/hello")
    public String hello() {
        return "Hello from spring boot!";
    }

    @GetMapping("/hello/{timeout}")
    public String getAnonymousInfo(@PathVariable Long timeout) throws InterruptedException {
        Thread.sleep(timeout);
        return "Hello from spring boot after " + timeout + " ms!";
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBook(@PathVariable Long id) {
        Optional<Book> book = service.findById(id);;
        return book.map(mapper::toDto).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/cached")
    public ResponseEntity<BookDto> getBookCached(@PathVariable Long id) {
        Optional<Book> book = service.findByIdCached(id);;
        return book.map(mapper::toDto).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/random/rows/{rows}/")
    public ResponseEntity<BookDto> getRandomBook(@PathVariable Integer rows) {
        Book book = service.findRandom(rows);
        return new ResponseEntity<>(mapper.toDto(book), new HttpHeaders(), HttpStatus.OK);
    }

    @GetMapping("/random/cached/rows/{rows}/")
    public ResponseEntity<BookDto> getRandomBookCached(@PathVariable Integer rows) {
        Book book = service.findRandomCached(rows);
        return new ResponseEntity<>(mapper.toDto(book), new HttpHeaders(), HttpStatus.OK);
    }

    @GetMapping("/random/rows/{rows}/limit/{limit}")
    public ResponseEntity<List<BookDto>> getRandomBooks(@PathVariable Integer rows, @PathVariable Integer limit) {
        List<Book> books = service.getRandomBooks(rows, limit);

        List<BookDto> bookDtoList = books.stream()
                .map(mapper::toDto)
                .toList();

        return new ResponseEntity<>(bookDtoList, new HttpHeaders(), HttpStatus.OK);
    }

    private List<BookDto> getBookDtos(List<Book> books) {
        return books.stream()
                .map(mapper::toDto)
                .toList();
    }

    @GetMapping("/paged/{pageNumber}/{pageSize}")
    public ResponseEntity<List<BookDto>> getBooks(@PathVariable Integer pageNumber, @PathVariable Integer pageSize) {
        Page< Book > data = service.getBookPagination(pageNumber, pageSize, null);
        return new ResponseEntity<>(getBookDtos(data.getContent()), new HttpHeaders(), HttpStatus.OK);
    }

    @GetMapping("/paged/{pageNumber}/{pageSize}/{sort}")
    public ResponseEntity<List<BookDto>> getBooks(@PathVariable Integer pageNumber, @PathVariable Integer pageSize, @PathVariable String sort) {
        Page < Book > data = service.getBookPagination(pageNumber, pageSize, sort);
        return new ResponseEntity<>(getBookDtos(data.getContent()), new HttpHeaders(), HttpStatus.OK);
    }

}
