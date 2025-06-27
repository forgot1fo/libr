package ru.library.library.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.library.library.model.Reader;
import ru.library.library.service.ReaderService;
import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/profile")
public class ReaderProfileController {

    private final ReaderService readerService;

    public ReaderProfileController(ReaderService readerService) {
        this.readerService = readerService;
    }

    @GetMapping
    public String showProfile(Model model, HttpSession session) {
        Long readerId = (Long) session.getAttribute("readerId");
        if (readerId == null) {
            return "redirect:/login";
        }

        Reader reader = readerService.getReaderById(readerId);
        if (reader == null) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute("reader", reader);
        model.addAttribute("success", model.getAttribute("success") != null ? model.getAttribute("success") : "");
        model.addAttribute("error", model.getAttribute("error") != null ? model.getAttribute("error") : "");
        model.addAttribute("readerId", session.getAttribute("readerId"));
        return "profile";
    }
    @PostMapping("/update-info")
    public String updateInfo(
            @RequestParam Long readerId,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String phone,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        Long sessionReaderId = (Long) session.getAttribute("readerId");
        if (sessionReaderId == null || !sessionReaderId.equals(readerId)) {
            return "redirect:/login";
        }

        // Валидация имени
        if (firstName.matches(".*\\d.*")) {
            redirectAttributes.addFlashAttribute("error", "Имя не должно содержать цифры");
            return "redirect:/profile";
        }

        // Валидация фамилии
        if (lastName.matches(".*\\d.*")) {
            redirectAttributes.addFlashAttribute("error", "Фамилия не должна содержать цифры");
            return "redirect:/profile";
        }

        // Валидация телефона
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        if (cleanPhone.length() != 11) {
            redirectAttributes.addFlashAttribute("error", "Номер телефона должен содержать 11 цифр");
            return "redirect:/profile";
        }

        try {
            Reader reader = readerService.getReaderById(readerId);
            if (reader == null) {
                redirectAttributes.addFlashAttribute("error", "Читатель не найден");
                return "redirect:/profile";
            }

            reader.setFirstName(firstName.trim());
            reader.setLastName(lastName.trim());
            reader.setPhone(cleanPhone); // Сохраняем очищенный номер

            readerService.save(reader);
            redirectAttributes.addFlashAttribute("success", "Данные успешно обновлены");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении данных: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    @PostMapping("/update-password")
    public String updatePassword(
            @RequestParam Long readerId,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        // Проверка авторизации
        Long sessionReaderId = (Long) session.getAttribute("readerId");
        if (sessionReaderId == null || !sessionReaderId.equals(readerId)) {
            return "redirect:/login";
        }

        // Валидация паролей
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Новый пароль и подтверждение не совпадают");
            redirectAttributes.addFlashAttribute("fieldError", "password");
            return "redirect:/profile";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Пароль должен содержать минимум 6 символов");
            redirectAttributes.addFlashAttribute("fieldError", "password");
            return "redirect:/profile";
        }

        Reader reader = readerService.getReaderById(readerId);
        if (reader == null) {
            redirectAttributes.addFlashAttribute("error", "Читатель не найден");
            return "redirect:/profile";
        }

        // Проверка текущего пароля
        if (!reader.getReaderPassword().equals(currentPassword)) {
            redirectAttributes.addFlashAttribute("error", "Текущий пароль неверен");
            redirectAttributes.addFlashAttribute("fieldError", "password");
            return "redirect:/profile";
        }

        // Обновление пароля
        try {
            reader.setReaderPassword(newPassword);
            readerService.save(reader);
            redirectAttributes.addFlashAttribute("success", "Пароль успешно изменен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при изменении пароля: " + e.getMessage());
            redirectAttributes.addFlashAttribute("fieldError", "password");
        }

        return "redirect:/profile";
    }
}