package ru.library.library;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // активируем тестовый профиль
class LibraryApplicationTests {

	@Test
	void contextLoads() {
		// проверка запуска контекста
	}
}
