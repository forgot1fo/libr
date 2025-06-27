package ru.library.library.controller;
        import jakarta.servlet.http.HttpSession;
        import lombok.RequiredArgsConstructor;
        import org.springframework.dao.DataIntegrityViolationException;
        import org.springframework.format.annotation.DateTimeFormat;
        import org.springframework.http.ResponseEntity;
        import org.springframework.stereotype.Controller;
        import org.springframework.ui.Model;
        import org.springframework.web.bind.annotation.*;
        import org.springframework.web.servlet.mvc.support.RedirectAttributes;
        import ru.library.library.model.Author;
        import ru.library.library.model.Book;
        import ru.library.library.service.AuthorService;

        import java.time.LocalDate;
        import java.time.format.DateTimeFormatter;
        import java.time.format.DateTimeParseException;
        import java.util.List;
        import java.util.Map;
        import java.util.UUID;
        import java.util.stream.Collectors;
        import org.springframework.validation.BindingResult;
@Controller
@RequestMapping("/authors")
public class AuthorViewController {
    private final AuthorService authorService;

    public AuthorViewController(AuthorService authorService) {
        this.authorService = authorService;
    }

    // Просмотр всех авторов
    @GetMapping
    public String showAuthorsPage(Model model, HttpSession session) {
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("readerId", session.getAttribute("readerId"));
        return "authors";
    }

    // Поиск авторов
    @GetMapping("/searchAuthor")
    public String searchAuthors(@RequestParam String query, Model model, HttpSession session) {
        model.addAttribute("authors", authorService.searchAuthorsByName(query));
        model.addAttribute("isSearchAuthor", true);
        model.addAttribute("readerId", session.getAttribute("readerId"));
        return "authors";
    }

    // Форма добавления нового автора
    @GetMapping("/newAuthor")
    public String showAddForm(Model model, HttpSession session) {
        model.addAttribute("author", new Author());
        model.addAttribute("readerId", session.getAttribute("readerId"));
        return "addAuthor";
    }

    // Сохранение нового автора
    @PostMapping("/saveAuthor")
    public String saveAuthor(@ModelAttribute Author author,
                             RedirectAttributes redirectAttributes,
                             Model model,
                             HttpSession session) {

        // Валидация имени
        if (!isValidName(author.getFirstName())) {
            redirectAttributes.addFlashAttribute("error", "Имя должно содержать только буквы и начинаться с заглавной");
            return "redirect:/authors/newAuthor";
        }

        // Валидация фамилии
        if (!isValidName(author.getLastName())) {
            redirectAttributes.addFlashAttribute("error", "Фамилия должна содержать только буквы и начинаться с заглавной");
            return "redirect:/authors/newAuthor";
        }

        // Валидация отчества
        if (!isValidName(author.getMiddleName())) {
            redirectAttributes.addFlashAttribute("error", "Отчество должно содержать только буквы и начинаться с заглавной");
            return "redirect:/authors/newAuthor";
        }

        // Валидация страны
        if (!isValidCountry(author.getCountry())) {
            redirectAttributes.addFlashAttribute("error", "Название страны должно содержать только буквы и начинаться с заглавной");
            return "redirect:/authors/newAuthor";
        }

        try {
            authorService.saveAuthor(author);
            redirectAttributes.addFlashAttribute("success", "Автор успешно добавлен!");
            return "redirect:/authors";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
            return "redirect:/authors/newAuthor";
        }
    }

    private boolean isValidName(String name) {
        return name != null && name.matches("^[А-ЯЁA-Z][а-яёa-z-]*");
    }

    private boolean isValidCountry(String country) {
        return country != null && country.matches("^[А-ЯЁA-Z][а-яёa-z\\s-]*");
    }
    private boolean isValidDate(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate.parse(date, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    // Форма редактирования автора
    @GetMapping("/editAuthor/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Author author = authorService.getAuthorById(id);
        if (author == null) {
            return "redirect:/authors";
        }
        model.addAttribute("author", author);
        return "editAuthor";
    }

    @PostMapping("/updateAuthor/{id}")
    public String updateAuthor(@PathVariable Long id,
                               @RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String middleName,
                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate birthday,
                               @RequestParam String country,
                               RedirectAttributes redirectAttributes) {

        // Валидация имени
        if (!isValidName(firstName)) {
            redirectAttributes.addFlashAttribute("error", "Имя должно содержать только буквы и начинаться с заглавной");
            return "redirect:/authors/editAuthor/" + id;
        }

        // Валидация фамилии
        if (!isValidName(lastName)) {
            redirectAttributes.addFlashAttribute("error", "Фамилия должна содержать только буквы и начинаться с заглавной");
            return "redirect:/authors/editAuthor/" + id;
        }

        // Валидация отчества
        if (!isValidName(middleName)) {
            redirectAttributes.addFlashAttribute("error", "Отчество должно содержать только буквы и начинаться с заглавной");
            return "redirect:/authors/editAuthor/" + id;
        }

        // Валидация страны
        if (!isValidCountry(country)) {
            redirectAttributes.addFlashAttribute("error", "Название страны должно содержать только буквы и начинаться с заглавной");
            return "redirect:/authors/editAuthor/" + id;
        }

        try {
            Author author = new Author();
            author.setAuthorId(id);
            author.setFirstName(firstName);
            author.setLastName(lastName);
            author.setMiddleName(middleName);
            author.setBirthday(birthday);
            author.setCountry(country);

            authorService.saveAuthor(author);
            redirectAttributes.addFlashAttribute("success", "Автор успешно обновлен!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении: " + e.getMessage());
        }

        return "redirect:/authors";
    }



    // Удаление автора
    @GetMapping("/deleteAuthor/{id}")
    public String deleteAuthor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        authorService.deleteAuthor(id);
        redirectAttributes.addFlashAttribute("message", "Автор успешно удален!");
        return "redirect:/authors";
    }

    // Удалены REST методы (оставлены только MVC методы)
}