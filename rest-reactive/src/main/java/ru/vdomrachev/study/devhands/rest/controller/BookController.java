package ru.vdomrachev.study.devhands.rest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vdomrachev.study.devhands.rest.dto.BookDto;
import ru.vdomrachev.study.devhands.rest.entity.Book;
import ru.vdomrachev.study.devhands.rest.mapper.BookMapper;
import ru.vdomrachev.study.devhands.rest.service.BookService;

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
    Mono<BookDto> retrieve(@PathVariable long id) {
        return service.retrieve(id).map(mapper::toDto);
    }

    @GetMapping("/{id}/cached")
    public Mono<BookDto> retrieveCached(@PathVariable Long id) {
        return service.retrieveCached(id).map(mapper::toDto);
    }

    @GetMapping("/random/{rows}")
    Mono<ResponseEntity<Book>> retrieveRandom(@PathVariable int rows) {
        return service.findRandom(rows)
                .map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/random/rows/{rows}/limit/{limit}")
    Flux<Book> getRandom(@PathVariable int rows, @PathVariable int limit) {
        return service.getRandom(rows, limit);
    }

}
