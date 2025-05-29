package ru.vdomrachev.study.devhands.rest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vdomrachev.study.devhands.rest.entity.Book;
import ru.vdomrachev.study.devhands.rest.mapper.BookMapper;
import ru.vdomrachev.study.devhands.rest.service.BookService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService service;

    private final BookMapper mapper;

    @GetMapping("/hello")
    public Mono<String> hello() {
        return Mono.just("Hello from spring boot!");
    }

    @GetMapping("/hello/{timeout}")
    public Mono<String> helloWithTimeout(@PathVariable Long timeout) throws InterruptedException {
        Thread.sleep(timeout);
        return Mono.just("Hello from spring boot!");
    }

    @GetMapping("/{id}")
    Mono<Book> retrieve(@PathVariable long id) {
        return service.retrieve(id);
    }

    @GetMapping("/{id}/cached")
    public Mono<Book> retrieveCached(@PathVariable Long id) {
        return service.retrieveCached(id);
    }

    @GetMapping("/random")
    Mono<ResponseEntity<Book>> retrieveRandom() {
        return service.findRandom()
                .map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/random/rows/{rows}")
    Mono<ResponseEntity<Book>> retrieveRandomWithRows(@PathVariable int rows) {
        return service.findRandom()
                .map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/random/limit/{limit}")
    Flux<List<Book>> getRandom(@PathVariable long limit) {
        return service.getRandomBooks(limit)
                .limitRate(10000)
                .buffer(10);
    }

    @GetMapping("/random/rows/{rows}/limit/{limit}")
    Flux<Book> getRandom(@PathVariable int rows, @PathVariable int limit) {
        return service.getRandomBooks(rows, limit);
    }

    @GetMapping("/random/rows/{rows}/limit/{limit}/cached")
    Flux<Book> getRandomCached(@PathVariable int rows, @PathVariable int limit) {
        return service.getRandomCached(rows, limit);
    }

}
