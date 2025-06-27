package ru.library.library.controller;
import org.slf4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.library.library.model.Book;
import ru.library.library.model.BookLoan;
import ru.library.library.model.Reader;
import ru.library.library.repository.ReaderRepository;
import ru.library.library.service.BookService;
import ru.library.library.service.AuthorService;
import ru.library.library.service.BookLoanService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import ru.library.library.service.ReaderService;
import org.springframework.validation.BindingResult;

@Slf4j
@Controller
@RequestMapping("/books")
public class BookViewController {
    private final BookService bookService;
    private final AuthorService authorService;
    private static final Logger log = LoggerFactory.getLogger(BookViewController.class);
    private final BookLoanService bookLoanService; // Добавлен новый сервис
    private final ReaderService readerService;
    public BookViewController(BookService bookService,
                              AuthorService authorService,
                              BookLoanService bookLoanService, ReaderRepository readerRepository, ReaderService readerService) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.bookLoanService = bookLoanService;

        this.readerService = readerService;
    }

    @GetMapping
    public String showBooksPage(Model model, HttpSession session) {
        List<Book> books = bookService.getAllBooks();
        model.addAttribute("books", books);

        Map<Long, Boolean> isInLoanTableMap = books.stream()
                .collect(Collectors.toMap(
                        Book::getBookId,
                        book -> bookService.isBookInLoanTable(book.getBookId())
                ));

        model.addAttribute("isInLoanTableMap", isInLoanTableMap);
        model.addAttribute("readerId", session.getAttribute("readerId"));
        return "books";
    }

    @GetMapping("/search")
    public String searchBooks(@RequestParam String query, Model model, HttpSession session) {
        List<Book> books = bookService.searchBooksByName(query);
        model.addAttribute("books", books);

        Map<Long, Boolean> isInLoanTableMap = books.stream()
                .collect(Collectors.toMap(
                        Book::getBookId,
                        book -> !book.getLoans().isEmpty()
                ));

        model.addAttribute("isInLoanTableMap", isInLoanTableMap);
        model.addAttribute("isSearch", true);
        model.addAttribute("readerId", session.getAttribute("readerId"));
        return "books";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("authors", authorService.findAll());
        return "add";
    }

    @PostMapping("/save")
    public String saveBook(@ModelAttribute Book book,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {

        if (book.getPublicationYear() == null || !book.getPublicationYear().matches("^\\d{4}$")) {
            bindingResult.rejectValue("publicationYear", "error.publicationYear", "Год издания должен содержать ровно 4 цифры");
        } else {
            try {
                int year = Integer.parseInt(book.getPublicationYear());
                int currentYear = LocalDate.now().getYear();
                if (year < 1000 || year > currentYear + 5) {
                    bindingResult.rejectValue("publicationYear", "error.publicationYear",
                            "Год издания должен быть между 1000 и " + (currentYear + 5));
                }
            } catch (NumberFormatException e) {
                bindingResult.rejectValue("publicationYear", "error.publicationYear", "Некорректный год издания");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("authors", authorService.findAll());
            return "add";
        }

        try {
            bookService.saveBook(book);
            redirectAttributes.addFlashAttribute("success", "Книга успешно добавлена!");
            return "redirect:/books";
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка: книга с таким ID уже существует");
            return "redirect:/books/new";
        }
    }
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id);
        if (book == null) {
            return "redirect:/books";
        }
        model.addAttribute("book", book);
        model.addAttribute("authors", authorService.findAll());
        return "edit";
    }

    @PostMapping("/update/{id}")
    public String updateBook(@PathVariable Long id,
                             @ModelAttribute Book bookDetails,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        if (bookDetails.getPublicationYear() == null || !bookDetails.getPublicationYear().matches("^\\d{4}$")) {
            bindingResult.rejectValue("publicationYear", "error.publicationYear", "Год издания должен содержать ровно 4 цифры");
        } else {
            try {
                int year = Integer.parseInt(bookDetails.getPublicationYear());
                int currentYear = LocalDate.now().getYear();
                if (year < 1000 || year > currentYear) {
                    bindingResult.rejectValue("publicationYear", "error.publicationYear",
                            "Год издания должен быть между 1000 и " + (currentYear));
                }
            } catch (NumberFormatException e) {
                bindingResult.rejectValue("publicationYear", "error.publicationYear", "Некорректный год издания");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("authors", authorService.findAll());
            return "edit";
        }

        try {
            Book existingBook = bookService.getBookById(id);
            existingBook.setName(bookDetails.getName());
            existingBook.setPublisher(bookDetails.getPublisher());
            existingBook.setPublicationYear(bookDetails.getPublicationYear());
            existingBook.setGenre(bookDetails.getGenre());
            existingBook.setAuthor(bookDetails.getAuthor());

            bookService.saveBook(existingBook);
            redirectAttributes.addFlashAttribute("success", "Книга успешно обновлена!");
            return "redirect:/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении: " + e.getMessage());
            return "redirect:/books/edit/" + id;
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookService.deleteBook(id);
        redirectAttributes.addFlashAttribute("message", "Книга успешно удалена!");
        return "redirect:/books";
    }
    @PostMapping("/reserve")
    @ResponseBody
    public ResponseEntity<String> reserveBooks(@RequestBody Map<String, List<Long>> request,
                                               HttpSession session) {
        Long readerId = (Long) session.getAttribute("readerId");
        log.info("Попытка бронирования книг. Reader ID: {}", readerId);

        if (readerId == null || readerId == 1) {
            log.warn("Попытка бронирования от неавторизованного пользователя");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Только читатели могут бронировать книги");
        }

        List<Long> bookIds = request.get("bookIds");
        if (bookIds == null || bookIds.isEmpty()) {
            log.warn("Не выбрано ни одной книги для бронирования");
            return ResponseEntity.badRequest()
                    .body("Не выбрано ни одной книги");
        }

        try {
            Reader reader = readerService.getReaderById(readerId);
            if (reader == null) {
                log.error("Читатель с ID {} не найден", readerId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Читатель не найден");
            }

            LocalDate currentDate = LocalDate.now();
            LocalDate returnDate = currentDate.plusMonths(2);

            for (Long bookId : bookIds) {
                if (bookLoanService.isBookInLoanTable(bookId)) {
                    log.warn("Книга с ID {} уже забронирована", bookId);
                    return ResponseEntity.badRequest()
                            .body("Книга с ID " + bookId + " уже забронирована");
                }
            }

            for (Long bookId : bookIds) {
                Book book = bookService.getBookById(bookId);
                if (book == null) {
                    log.warn("Книга с ID {} не найдена", bookId);
                    return ResponseEntity.badRequest()
                            .body("Книга с ID " + bookId + " не найдена");
                }

                BookLoan loan = new BookLoan();
                loan.setBook(book);
                loan.setReader(reader);
                loan.setLoanDate(currentDate);
                loan.setReturnDate(returnDate);

                bookLoanService.saveLoan(loan);
                log.info("Книга с ID {} успешно забронирована для читателя {}", bookId, readerId);
            }

            return ResponseEntity.ok("Книги успешно забронированы");

        } catch (Exception e) {
            log.error("Ошибка при бронировании книг", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при бронировании: " + e.getMessage());
        }
    }

}