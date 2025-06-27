import lombok.Data;
import java.time.LocalDate;

@Data
public class BookLoanRequest {
    private LocalDate loanDate;
    private LocalDate returnDate;
    private Long bookId;
    private Long readerId;
}