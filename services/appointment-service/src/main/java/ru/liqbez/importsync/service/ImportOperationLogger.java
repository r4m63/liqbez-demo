package ru.liqbez.importsync.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.liqbez.importsync.model.ImportOperation;
import ru.liqbez.importsync.repository.ImportOperationRepository;

@Service
public class ImportOperationLogger {
    private final ImportOperationRepository repository;

    public ImportOperationLogger(ImportOperationRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void started(UUID id, String fileName) {
        repository.create(id, fileName);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completed(UUID id, int count, String objectKey) {
        repository.complete(id, count, objectKey);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failed(UUID id, String status) {
        repository.fail(id, status);
    }

    public ImportOperation find(UUID id) {
        return repository.find(id);
    }
}
