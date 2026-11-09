package ru.liqbez.appointment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {
    private final JdbcTemplate jdbc;

    public ReservationService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional
    public Reservation reserve(String id) {
        jdbc.update("MERGE INTO reservation (id, status) KEY (id) VALUES (?, 'RESERVED')", id);
        return new Reservation(id, "RESERVED");
    }

    @Transactional
    public Reservation cancel(String id) {
        jdbc.update("UPDATE reservation SET status = 'CANCELLED' WHERE id = ? AND status = 'RESERVED'", id);
        return find(id);
    }

    public Reservation find(String id) {
        return jdbc.queryForObject("SELECT id, status FROM reservation WHERE id = ?", (rs, row) ->
                new Reservation(rs.getString("id"), rs.getString("status")), id);
    }

    public record Reservation(String id, String status) {}
}
