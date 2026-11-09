package ru.liqbez.importsync.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.liqbez.importsync.repository.ImportedVisitRepository;
import ru.liqbez.importsync.storage.ObjectStorage;

@Service
public class PetVisitImportTxService {
    private static final Logger log = LoggerFactory.getLogger(PetVisitImportTxService.class);

    private final ImportedVisitRepository visits;
    private final ImportOperationLogger operationLogger;
    private final ObjectStorage storage;

    public PetVisitImportTxService(ImportedVisitRepository visits,
                                   ImportOperationLogger operationLogger,
                                   ObjectStorage storage) {
        this.visits = visits;
        this.operationLogger = operationLogger;
        this.storage = storage;
    }

    @Transactional
    public void importVisits(UUID id, List<String> petNames, byte[] fileBytes,
                             String objectKey, boolean failAfterUpload) {
        AtomicBoolean uploaded = new AtomicBoolean();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void beforeCommit(boolean readOnly) {
                storage.put(objectKey, fileBytes);
                uploaded.set(true);
                if (failAfterUpload) throw new IllegalStateException("Injected failure after object upload");
            }

            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    updateOperation(() -> operationLogger.completed(id, petNames.size(), objectKey), id);
                    return;
                }
                if (status == STATUS_ROLLED_BACK) {
                    boolean cleanupFailed = false;
                    if (uploaded.get()) {
                        try {
                            storage.remove(objectKey);
                        } catch (RuntimeException ex) {
                            cleanupFailed = true;
                            log.error("Could not compensate object upload for import {}", id, ex);
                        }
                    }
                    String finalStatus = cleanupFailed ? "CLEANUP_FAILED" : "FAILED";
                    updateOperation(() -> operationLogger.failed(id, finalStatus), id);
                    return;
                }
                updateOperation(() -> operationLogger.failed(id, "UNKNOWN"), id);
            }
        });

        visits.save(id, petNames);
    }

    private void updateOperation(Runnable update, UUID id) {
        try {
            update.run();
        } catch (RuntimeException ex) {
            log.error("Could not update status for import {}", id, ex);
        }
    }
}
