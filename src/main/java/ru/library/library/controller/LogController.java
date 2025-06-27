package ru.library.library.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.library.library.model.Reader;
import ru.library.library.repository.ReaderRepository;
import org.springframework.ui.Model;

import java.time.LocalDate;

@Controller
public class LogController {

    private final ReaderRepository readerRepository;

    public LogController(ReaderRepository readerRepository) {
        this.readerRepository = readerRepository;
    }

    // ========== Вход ==========
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String readerPassword, HttpSession session,
                        Model model) {
        Reader reader = readerRepository.findByEmail(email);

        if (reader == null || !reader.getReaderPassword().equals(readerPassword)) {
            model.addAttribute("loginError", true);
            return "login";
        }

        // Успешный вход (в реальном приложении — сохранять в сессию и т.п.)
        session.setAttribute("readerId", reader.getReaderId());
        if (reader.getReaderId() == 1) {
            return "redirect:/bookloans";  // доступ к таблице readers
        } else {
            return "redirect:/books";  // все остальные — на bookloans
        }
    }

    // ========== Регистрация ==========
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("reader", new Reader());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute Reader reader, Model model) {
        if (readerRepository.existsByEmail(reader.getEmail())) {
            model.addAttribute("emailExists", true);
            return "register";
        }

        reader.setRegistrationDate(LocalDate.now());
        reader.setActive(true);
        readerRepository.save(reader);

        return "redirect:/login";
    }
}
