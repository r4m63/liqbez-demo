package ru.liqbez.saga.controller;

import org.springframework.web.bind.annotation.*;
import ru.liqbez.saga.model.SagaResult;
import ru.liqbez.saga.service.SagaService;

@RestController
@RequestMapping("/transactions/saga")
public class SagaDemoController {
    private final SagaService service;

    public SagaDemoController(SagaService service) { this.service = service; }

    @PostMapping("/{id}")
    public SagaResult run(@PathVariable String id,
            @RequestParam(defaultValue = "false") boolean failPayment) {
        return service.run(id, failPayment);
    }

    @GetMapping("/{id}")
    public SagaResult find(@PathVariable String id) { return service.find(id); }
}
