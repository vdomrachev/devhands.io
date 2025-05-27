package ru.vdomrachev.study.devhands.rest.controller;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ru.vdomrachev.study.devhands.rest.dto.BookDto;
import ru.vdomrachev.study.devhands.rest.entity.Book;
import ru.vdomrachev.study.devhands.rest.mapper.BookMapper;
import ru.vdomrachev.study.devhands.rest.service.BookService;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService service;

    private final BookMapper mapper;

    private final Random random = new Random(42);

    ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    @GetMapping("/hello")
    public String hello() {
        return "Hello from spring boot!";
    }

    @GetMapping("/hello/sleep/{timeout}")
    public String helloWithSleep(@PathVariable Long timeout) throws InterruptedException {

        log.info("Current Thread Name: {}", Thread.currentThread().getName());
        log.info("Current Thread ID: {}", Thread.currentThread().threadId());

        Thread.sleep(timeout);
        return "Hello from spring boot after " + timeout + " ms!";
    }

    @GetMapping("/hello/sleep/{timeout}/locked")
    public String helloWithSleepLocked(@PathVariable Long timeout) throws InterruptedException {
        synchronized (new Object()) {
            Thread.sleep(timeout);
        }
        return "Hello from spring boot after " + timeout + " ms!";
    }

    @GetMapping("/hello/consume/{interval}")
    public String helloAndConsumeCpu(@PathVariable Long interval) throws InterruptedException {
        consumeCpu(interval);
        return "Hello from spring boot after " + interval + " ms!";
    }

    private void consumeCpu(Long interval) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        long userStatTime = threadBean.getCurrentThreadUserTime(); // время в пользовательском режиме в наносекундах

        long endTime = userStatTime + interval * 1_000_000;
        while (threadBean.getCurrentThreadUserTime() < endTime) {
            for (int i = 0; i < 10000; i++) {
                int randomNumber = random.nextInt();
                String randomString = String.valueOf(randomNumber);
                md.digest(randomString.getBytes());
            }
        }
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
