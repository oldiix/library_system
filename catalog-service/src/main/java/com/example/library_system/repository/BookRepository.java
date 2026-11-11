package com.example.library_system.repository;

import com.example.library_system.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByAuthorId(Long authorId);

    List<Book> findByAvailableCopiesLessThanEqualOrderByTitleAsc(Integer threshold);
}