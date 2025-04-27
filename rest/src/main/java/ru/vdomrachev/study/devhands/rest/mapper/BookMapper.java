package ru.vdomrachev.study.devhands.rest.mapper;

import org.mapstruct.Mapper;

import ru.vdomrachev.study.devhands.rest.dto.BookDto;
import ru.vdomrachev.study.devhands.rest.entity.Book;

@Mapper(componentModel = "spring")
public interface BookMapper {
	BookDto toDto(Book book);
}
