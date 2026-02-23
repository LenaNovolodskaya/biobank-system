package ru.healthfamily.biobank.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String DUPLICATE_TEMPLATE_MSG = "Такой шаблон контейнера уже существует";
    private static final String TEMPLATE_IN_USE_MSG = "Шаблон нельзя удалить: он используется в контейнерах";

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Неверное имя пользователя или пароль"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "Ошибка валидации"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();
        if (message != null && message.contains("уже существует")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", message));
        }
        throw ex;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
        if (msg != null) {
            if (msg.contains("duplicate") || msg.contains("23505") || msg.contains("template_name")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", DUPLICATE_TEMPLATE_MSG));
            }
            if (msg.contains("23503") || msg.contains("storage_containers") || msg.contains("violates foreign key")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", TEMPLATE_IN_USE_MSG));
            }
        }
        throw ex;
    }
}
