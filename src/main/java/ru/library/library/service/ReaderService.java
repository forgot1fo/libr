package ru.library.library.service;

import org.springframework.stereotype.Service;
import ru.library.library.model.Book;
import ru.library.library.model.Reader;
import ru.library.library.repository.ReaderRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ReaderService {

    private final ReaderRepository readerRepository;

    public ReaderService(ReaderRepository readerRepository) {
        this.readerRepository = readerRepository;
    }
    public Reader getReaderById(Long readerId) {
        return readerRepository.findById(readerId).orElse(null);
    }
    public List<Reader> findAll() {
        return readerRepository.findAll();
    }

    public Optional<Reader> findById(Long id) {
        return readerRepository.findById(id);
    }

    public Reader save(Reader reader) {
        return readerRepository.save(reader);
    }

    public void deleteById(Long id) {
        readerRepository.deleteById(id);
    }
    public List<Reader> findByLastNameContaining(String lastName) {
        return readerRepository.findByLastNameContainingIgnoreCase(lastName);
    }

    public void toggleActiveStatus(Long id) {
        Reader reader = readerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Читатель не найден"));
        reader.setActive(!reader.getActive());
        readerRepository.save(reader);
    }

    public void deleteReader(Long id) {
        readerRepository.deleteById(id);
    }
}
