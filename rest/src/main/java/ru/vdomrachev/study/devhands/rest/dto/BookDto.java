package ru.vdomrachev.study.devhands.rest.dto;

/**
 * DTO for {@link ru.vdomrachev.study.devhands.rest.entity.Book}
 */
public record BookDto(Long id, String name, String author, String isbn) {
}