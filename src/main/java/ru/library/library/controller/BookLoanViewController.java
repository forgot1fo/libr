package ru.library.library.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.library.library.model.BookLoan;
import ru.library.library.service.BookLoanService;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/bookloans")
public class BookLoanViewController {
    private final BookLoanService bookLoanService;

    public BookLoanViewController(BookLoanService bookLoanService) {
        this.bookLoanService = bookLoanService;
    }
    @GetMapping
    public String showBooksPage(Model model, HttpSession session,
                                @RequestParam(required = false) String search) {
        Long currentReaderId = (Long) session.getAttribute("readerId");

        if (currentReaderId == null) {
            return "redirect:/login";
        }

        List<BookLoan> loans;
        if (currentReaderId == 1L) {
            // Администратор (id=1) видит все записи или результаты поиска
            if (search != null && !search.isEmpty()) {
                loans = bookLoanService.searchByReaderLastName(search);
            } else {
                loans = bookLoanService.getAllBookLoans();
            }
        } else {
            // Обычный пользователь видит только свои записи
            loans = bookLoanService.getBookLoansByReaderId(currentReaderId);
        }

        model.addAttribute("bookloans", loans);
        model.addAttribute("isAdmin", currentReaderId == 1L);
        model.addAttribute("readerId", session.getAttribute("readerId"));
        return "bookloans";
    }

    @GetMapping("/delete/{id}")
    public String deleteLoan(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookLoanService.deleteLoan(id);
        redirectAttributes.addFlashAttribute("message", "Запись успешно удалена!");
        return "redirect:/bookloans";
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookLoan createBookLoan(
            @RequestParam LocalDate loanDate,
            @RequestParam LocalDate returnDate,
            @RequestParam Long bookId,
            @RequestParam Long readerId) {
        return bookLoanService.createBookLoan(loanDate, returnDate, bookId, readerId);
    }
    @PostMapping("/toggle-status/{id}")
    public ResponseEntity<String> toggleLoanStatus(
            @PathVariable Long id,
            @RequestParam boolean activeLoan) {

        bookLoanService.updateLoanStatus(id, activeLoan);
        return ResponseEntity.ok().build();
    }
}