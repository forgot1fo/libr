# Используем официальный образ Java 17
FROM eclipse-temurin:17-jdk as builder

# Рабочая директория
WORKDIR /app

# Копируем только pom.xml сначала (для кэширования зависимостей)
COPY pom.xml .
# Скачиваем зависимости
RUN mvn dependency:go-offline

# Копируем исходный код
COPY src ./src

# Собираем JAR-файл
RUN mvn clean package -DskipTests

# Финальный образ
FROM eclipse-temurin:17-jre

WORKDIR /app

# Копируем собранный JAR из стадии builder
COPY --from=builder /app/target/*.jar app.jar

# Открываем порт
EXPOSE 8080

# Команда запуска
ENTRYPOINT ["java", "-jar", "app.jar"]