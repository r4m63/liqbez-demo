package ru.liqbez.xa.controller;

import java.util.UUID;
import org.springframework.web.bind.annotation.*;
import ru.liqbez.xa.model.XaResult;
import ru.liqbez.xa.service.XaTwoPhaseCommitService;

@RestController
@RequestMapping("/transactions/xa")
public class XaDemoController {
    private final XaTwoPhaseCommitService service;

    public XaDemoController(XaTwoPhaseCommitService service) { this.service = service; }

    @PostMapping("/{id}")
    public XaResult run(@PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean failBeforePrepare) throws Exception {
        return service.run(id, failBeforePrepare);
    }

    @GetMapping("/{id}")
    public XaResult find(@PathVariable UUID id) throws Exception {
        return service.find(id);
    }
}
