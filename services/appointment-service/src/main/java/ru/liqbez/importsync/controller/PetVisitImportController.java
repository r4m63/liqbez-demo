package ru.liqbez.importsync.controller;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.liqbez.importsync.model.ImportRequest;
import ru.liqbez.importsync.model.ImportState;
import ru.liqbez.importsync.service.PetVisitImportService;

@RestController
@RequestMapping("/transactions/synchronization/imports")
public class PetVisitImportController {
    private final PetVisitImportService service;

    public PetVisitImportController(PetVisitImportService service) {
        this.service = service;
    }

    @PostMapping("/{id}")
    public ImportState importVisits(@PathVariable UUID id, @RequestBody ImportRequest request,
                                    @RequestParam(defaultValue = "false") boolean failAfterUpload) {
        return service.importVisits(id, request, failAfterUpload);
    }

    @GetMapping("/{id}")
    public ImportState find(@PathVariable UUID id) {
        return service.find(id);
    }
}
