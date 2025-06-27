
package ru.library.library.controller;

        import jakarta.servlet.http.HttpSession;
        import org.springframework.http.ResponseEntity;
        import org.springframework.stereotype.Controller;
        import org.springframework.ui.Model;
        import org.springframework.web.bind.annotation.*;
        import ru.library.library.model.Reader;
        import ru.library.library.service.ReaderService;

        import java.util.List;

@Controller
@RequestMapping("/readers")
public class ReaderViewController{

    private final ReaderService readerService;

    public ReaderViewController(ReaderService readerService) {
        this.readerService = readerService;
    }

    @GetMapping
    public String showAllReaders(Model model, HttpSession session) {
        Long readerId = (Long) session.getAttribute("readerId");
        if (readerId == null) {
            return "redirect:/login";
        }

        model.addAttribute("readers", readerService.findAll());
        model.addAttribute("readerId", readerId);
        return "readers";
    }
    @GetMapping("/{id}")
    public ResponseEntity<Reader> getReaderById(@PathVariable Long id) {
        return readerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Reader> createReader(@RequestBody Reader reader) {
        return ResponseEntity.ok(readerService.save(reader));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reader> updateReader(@PathVariable Long id, @RequestBody Reader reader) {
        return readerService.findById(id)
                .map(existingReader -> {
                    //reader.setReaderId(id);
                    return ResponseEntity.ok(readerService.save(reader));
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/search")
    public String searchReaders(@RequestParam String lastName, Model model) {
        model.addAttribute("readers", readerService.findByLastNameContaining(lastName));
        model.addAttribute("lastName", lastName); // Добавляем параметр в модель
        return "readers";
    }

    @PostMapping("/toggle-active/{id}")
    public String toggleActiveStatus(@PathVariable Long id) {
        readerService.toggleActiveStatus(id);
        return "redirect:/readers";
    }

    @GetMapping("/delete/{id}")
    public String deleteReader(@PathVariable Long id) {
        readerService.deleteReader(id);
        return "redirect:/readers";
    }

}