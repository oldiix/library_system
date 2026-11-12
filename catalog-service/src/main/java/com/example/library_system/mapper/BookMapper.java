package com.example.library_system.mapper;

import com.example.library_system.dto.BookDto;
import com.example.library_system.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookMapper {

    @Mapping(source = "author.id", target = "authorId")
    @Mapping(target = "authorName",
            expression = "java(book.getAuthor().getFirstName() + \" \" + book.getAuthor().getLastName())")
    BookDto toDto(Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    Book toEntity(BookDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    void updateEntity(BookDto dto, @MappingTarget Book book);
}