
package ru.library.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.library.library.model.Reader;

import java.util.List;

public interface ReaderRepository extends JpaRepository<Reader, Long> {
    boolean existsByEmail(String email);
    Reader findByEmail(String email);
    List<Reader> findByLastNameContainingIgnoreCase(String lastName);

}