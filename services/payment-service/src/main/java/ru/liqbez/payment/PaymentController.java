package ru.liqbez.payment;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final JdbcTemplate jdbc;

    public PaymentController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @PostMapping("/{id}")
    @Transactional
    public Payment charge(@PathVariable String id, @RequestParam(defaultValue = "false") boolean fail) {
        if (fail) throw new ResponseStatusException(HttpStatus.CONFLICT, "Demo payment rejection");
        jdbc.update("MERGE INTO payment (id, status) KEY (id) VALUES (?, 'CHARGED')", id);
        return new Payment(id, "CHARGED");
    }

    @GetMapping("/{id}")
    public Payment find(@PathVariable String id) {
        return jdbc.queryForObject("SELECT id, status FROM payment WHERE id = ?", (rs, row) ->
                new Payment(rs.getString("id"), rs.getString("status")), id);
    }

    public record Payment(String id, String status) {}
}
