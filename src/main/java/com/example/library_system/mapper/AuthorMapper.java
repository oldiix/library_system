package com.example.library_system.mapper;

import com.example.library_system.dto.AuthorDto;
import com.example.library_system.entity.Author;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AuthorMapper {

    AuthorDto toDto(Author author);

    @Mapping(target = "id", ignore = true)
    Author toEntity(AuthorDto dto);

    @Mapping(target = "id", ignore = true)
    void updateEntity(AuthorDto dto, @MappingTarget Author author);
}
