package ru.library.library.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.library.library.model.Book;
import ru.library.library.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
@Slf4j
public class BookService {
    private final BookRepository bookRepository;
    private static final Logger log = LoggerFactory.getLogger(BookService.class);
    private final BookLoanService bookLoanService;
    @Autowired
    public BookService(BookRepository bookRepository, BookLoanService bookLoanService) {
        this.bookRepository = bookRepository;
        this.bookLoanService = bookLoanService;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> searchBooksByName(String name) {
        return bookRepository.findByNameContainingIgnoreCase(name);
    }
    public boolean isBookInLoanTable(Long bookId) {
        try {
            Book book = bookRepository.findById(bookId).orElse(null);
            boolean isLoaned = book != null && !book.getLoans().isEmpty();
            log.debug("Check book {} availability: {}", bookId, isLoaned ? "not available" : "available");
            return isLoaned;
        } catch (Exception e) {
            log.error("Error checking book availability", e);
            return true; // в случае ошибки считаем книгу недоступной
        }
    }
   @Transactional
   public Book saveBook(Book book) {
       // Если книга новая (без ID) - сохраняем как новую
       if (book.getBookId() == null) {
           return bookRepository.save(book);
       }

       // Если книга существует - обновляем
       Book existingBook = bookRepository.findById(book.getBookId())
               .orElseThrow(() -> new IllegalArgumentException("Книга не найдена"));

       existingBook.setName(book.getName());
       existingBook.setPublisher(book.getPublisher());
       existingBook.setPublicationYear(book.getPublicationYear());
       existingBook.setGenre(book.getGenre());
       existingBook.setAuthor(book.getAuthor());

       return bookRepository.save(existingBook);
   }
    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }
}