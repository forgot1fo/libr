package ru.library.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.library.library.model.BookLoan;
import ru.library.library.model.Reader;

import java.time.LocalDate;
import java.util.List;

public interface BookLoanRepository extends JpaRepository<BookLoan, Long> {
    List<BookLoan> findByReaderReaderId(Long readerId);
    @Query("SELECT CASE WHEN COUNT(bl) > 0 THEN true ELSE false END " +
            "FROM BookLoan bl WHERE bl.book.id = :bookId AND bl.returnDate > :currentDate")
    boolean existsByBookIdAndReturnDateAfter(@Param("bookId") Long bookId,
                                             @Param("currentDate") LocalDate currentDate);
    boolean existsByBook_BookIdAndReturnDateIsNull(Long bookId);
    List<BookLoan> findByReaderLastNameContainingIgnoreCase(String lastName);

}