package ru.liqbez.appointment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appointments/reservations")
public class ReservationController {
    private final JdbcTemplate jdbc;

    public ReservationController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @PostMapping("/{id}")
    @Transactional
    public Reservation reserve(@PathVariable String id) {
        jdbc.update("MERGE INTO reservation (id, status) KEY (id) VALUES (?, 'RESERVED')", id);
        return new Reservation(id, "RESERVED");
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Reservation cancel(@PathVariable String id) {
        jdbc.update("UPDATE reservation SET status = 'CANCELLED' WHERE id = ? AND status = 'RESERVED'", id);
        return find(id);
    }

    @GetMapping("/{id}")
    public Reservation find(@PathVariable String id) {
        return jdbc.queryForObject("SELECT id, status FROM reservation WHERE id = ?", (rs, row) ->
                new Reservation(rs.getString("id"), rs.getString("status")), id);
    }

    public record Reservation(String id, String status) {}
}
