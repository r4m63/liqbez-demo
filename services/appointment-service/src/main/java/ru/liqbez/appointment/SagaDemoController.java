package ru.liqbez.appointment;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions/saga")
public class SagaDemoController {
    private final SagaService service;

    public SagaDemoController(SagaService service) { this.service = service; }

    @PostMapping("/{id}")
    public SagaService.SagaResult run(@PathVariable String id,
            @RequestParam(defaultValue = "false") boolean failPayment) {
        return service.run(id, failPayment);
    }

    @GetMapping("/{id}")
    public SagaService.SagaResult find(@PathVariable String id) { return service.find(id); }
}
