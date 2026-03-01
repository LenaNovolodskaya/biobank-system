package ru.healthfamily.biobank.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.healthfamily.biobank.model.TransactionType;
import ru.healthfamily.biobank.repository.TransactionTypeRepository;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(100)
public class TransactionTypeInitializer implements CommandLineRunner {

    private final TransactionTypeRepository transactionTypeRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Инициализация справочника типов операций...");
        createIfAbsent("Создан");
        createIfAbsent("Изъвлечён из хранилища");
        createIfAbsent("Возвращён в хранилище");
        createIfAbsent("Исчерпан");
        log.info("Справочник типов операций инициализирован");
    }

    private void createIfAbsent(String name) {
        if (transactionTypeRepository.findByTransactionTypeNameIgnoreCase(name).isEmpty()) {
            TransactionType t = new TransactionType();
            t.setTransactionTypeName(name);
            transactionTypeRepository.save(t);
        }
    }
}
