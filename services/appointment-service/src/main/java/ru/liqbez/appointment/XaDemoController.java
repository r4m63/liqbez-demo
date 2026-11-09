package ru.liqbez.appointment;

import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions/xa")
public class XaDemoController {
    private final XaTwoPhaseCommitService service;

    public XaDemoController(XaTwoPhaseCommitService service) { this.service = service; }

    @PostMapping("/{id}")
    public XaTwoPhaseCommitService.XaResult run(@PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean failBeforePrepare) throws Exception {
        return service.run(id, failBeforePrepare);
    }

    @GetMapping("/{id}")
    public XaTwoPhaseCommitService.XaResult find(@PathVariable UUID id) throws Exception {
        return service.find(id);
    }
}
