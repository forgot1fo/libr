package ru.library.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.library.library.model.Author;
import ru.library.library.model.Book;
import ru.library.library.repository.AuthorRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    @Autowired
    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public List<Author> findAll() {
        return authorRepository.findAll();
    }

    public Optional<Author> findById(Long id) {
        return authorRepository.findById(id);
    }
    public Author getAuthorById(Long id) {
        return authorRepository.findById(id).orElse(null);
    }

    public Author save(Author author) {
        return authorRepository.save(author);
    }

    public void delete(Long id) {
        authorRepository.deleteById(id);
    }
    public List<Author> searchAuthorsByName(String lastname) {
        return authorRepository.findByLastNameContainingIgnoreCase(lastname);
    }

    @Transactional
    public Author saveAuthor(Author author) {
        // Если книга новая (без ID) - сохраняем как новую
        if (author.getAuthorId() == null) {
            return authorRepository.save(author);
        }

        // Если книга существует - обновляем
        Author existingAuthor = authorRepository.findById(author.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("Автор не найден"));

        existingAuthor.setFirstName(author.getFirstName());
        existingAuthor.setLastName(author.getLastName());
        existingAuthor.setMiddleName(author.getMiddleName());
        existingAuthor.setBirthday(author.getBirthday());
        existingAuthor.setCountry(author.getCountry());

        return authorRepository.save(existingAuthor);
    }
    public void deleteAuthor(Long id) {
        authorRepository.deleteById(id);
    }
}