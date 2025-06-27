package ru.library.library.service;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.library.library.model.Book;
import ru.library.library.model.BookLoan;
import ru.library.library.model.Reader;
import ru.library.library.repository.*;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookLoanService {
    private final BookLoanRepository bookLoanRepository;
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    public boolean isBookInLoanTable(Long bookId) {
        return bookLoanRepository.existsByBook_BookIdAndReturnDateIsNull(bookId);
    }

    public BookLoanService(BookLoanRepository bookLoanRepository, BookRepository bookRepository, ReaderRepository readerRepository) {
        this.bookLoanRepository = bookLoanRepository;
        this.bookRepository = bookRepository;
        this.readerRepository = readerRepository;
    }
    @Transactional
    public void saveLoan(BookLoan loan) {
        bookLoanRepository.save(loan);
    }

    public boolean isBookLoaned(Long bookId) {
        return bookLoanRepository.existsByBookIdAndReturnDateAfter(bookId, LocalDate.now());
    }
    public List<BookLoan> getAllBookLoans() {
        return bookLoanRepository.findAll();
    }
    public List<BookLoan> getBookLoansByReaderId(Long readerId) {
        return bookLoanRepository.findByReaderReaderId(readerId);
    }
    @Transactional
    public BookLoan createBookLoan(LocalDate loanDate, LocalDate returnDate,
                                   Long bookId, Long readerId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        Reader reader = readerRepository.findById(readerId)
                .orElseThrow(() -> new RuntimeException("Reader not found"));

        BookLoan loan = new BookLoan();
        loan.setLoanDate(loanDate);
        loan.setReturnDate(returnDate);
        loan.setBook(book);
        loan.setReader(reader);

        return bookLoanRepository.save(loan);
    }
    public List<BookLoan> searchByReaderLastName(String lastName) {
        return bookLoanRepository.findByReaderLastNameContainingIgnoreCase(lastName);
    }
    @Transactional
    public ResponseEntity<String> reserveBooks(List<Long> bookIds, Reader reader, LocalDate loanDate, LocalDate returnDate) {
        for (Long bookId : bookIds) {
            if (isBookInLoanTable(bookId)) {
                return ResponseEntity.badRequest()
                        .body("Книга с ID " + bookId + " уже забронирована");
            }
        }

        for (Long bookId : bookIds) {
            Book book = bookRepository.findById(bookId).orElse(null);
            if (book == null) {
                return ResponseEntity.badRequest()
                        .body("Книга с ID " + bookId + " не найдена");
            }

            BookLoan loan = new BookLoan();
            loan.setBook(book);
            loan.setReader(reader);
            loan.setLoanDate(loanDate);
            loan.setReturnDate(returnDate);

            bookLoanRepository.save(loan);
        }

        return ResponseEntity.ok("Книги успешно забронированы");
    }
    @Transactional
    public void deleteLoan(Long id) {
        if (!bookLoanRepository.existsById(id)) {
            throw new RuntimeException("Запись о выдаче книги не найдена");
        }
        bookLoanRepository.deleteById(id);
    }
    @Transactional
    public void updateLoanStatus(Long loanId, boolean activeLoan) {
        BookLoan loan = bookLoanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Запись о выдаче не найдена"));
        loan.setActiveLoan(activeLoan);
    }
}