package ru.vdomrachev.study.devhands.rest.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Table("book")
public class Book {
    @Id
    private Long id;

    private String name;
    private String author;
    private String isbn;
}