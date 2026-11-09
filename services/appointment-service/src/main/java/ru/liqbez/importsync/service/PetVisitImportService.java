package ru.liqbez.importsync.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.liqbez.importsync.model.ImportOperation;
import ru.liqbez.importsync.model.ImportRequest;
import ru.liqbez.importsync.model.ImportState;
import ru.liqbez.importsync.repository.ImportedVisitRepository;
import ru.liqbez.importsync.storage.ObjectStorage;

@Service
public class PetVisitImportService {
    private final ImportOperationLogger operationLogger;
    private final PetVisitImportTxService transactionService;
    private final ImportedVisitRepository visits;
    private final ObjectStorage storage;

    public PetVisitImportService(ImportOperationLogger operationLogger,
                                 PetVisitImportTxService transactionService,
                                 ImportedVisitRepository visits,
                                 ObjectStorage storage) {
        this.operationLogger = operationLogger;
        this.transactionService = transactionService;
        this.visits = visits;
        this.storage = storage;
    }

    public ImportState importVisits(UUID id, ImportRequest request, boolean failAfterUpload) {
        String fileName = request == null || request.fileName() == null || request.fileName().isBlank()
                ? "visits.txt" : request.fileName().replace('\\', '_').replace('/', '_')
                        .replace('\r', ' ').replace('\n', ' ').trim();
        if (fileName.isBlank()) fileName = "visits.txt";
        if (fileName.length() > 255) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File name too long");
        operationLogger.started(id, fileName);

        List<String> petNames = request == null ? null : request.petNames();
        if (petNames == null || petNames.isEmpty() || petNames.stream().anyMatch(name -> name == null || name.isBlank())) {
            operationLogger.failed(id, "FAILED");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide at least one non-blank pet name");
        }

        String objectKey = "imports/" + id + ".txt";
        byte[] fileBytes = String.join("\n", petNames).getBytes(StandardCharsets.UTF_8);
        transactionService.importVisits(id, petNames, fileBytes, objectKey, failAfterUpload);
        return find(id);
    }

    public ImportState find(UUID id) {
        ImportOperation operation = operationLogger.find(id);
        return new ImportState(operation, visits.findNames(id),
                storage.exists("imports/" + id + ".txt"));
    }
}
