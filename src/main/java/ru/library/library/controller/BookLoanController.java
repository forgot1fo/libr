package ru.library.library.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.library.library.model.BookLoan;
import ru.library.library.service.BookLoanService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookloans")
public class BookLoanController {
    private final BookLoanService bookLoanService;

    public BookLoanController(BookLoanService bookLoanService) {
        this.bookLoanService = bookLoanService;
    }

    // GET для получения всех выдач
    @GetMapping
    public List<BookLoan> getAllBookLoans() {
        return bookLoanService.getAllBookLoans();
    }

    // POST для создания новой выдачи
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookLoan createBookLoan(
            @RequestParam LocalDate loanDate,
            @RequestParam LocalDate returnDate,
            @RequestParam Long bookId,
            @RequestParam Long readerId) {
        return bookLoanService.createBookLoan(loanDate, returnDate, bookId, readerId);
    }
}