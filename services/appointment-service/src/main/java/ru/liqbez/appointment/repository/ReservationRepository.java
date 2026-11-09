package ru.liqbez.appointment.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.liqbez.appointment.model.Reservation;

@Repository
public class ReservationRepository {
    private final JdbcTemplate jdbc;

    public ReservationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void reserve(String id) {
        jdbc.update("MERGE INTO reservation (id, status) KEY (id) VALUES (?, 'RESERVED')", id);
    }

    public void cancel(String id) {
        jdbc.update("UPDATE reservation SET status = 'CANCELLED' WHERE id = ? AND status = 'RESERVED'", id);
    }

    public Reservation find(String id) {
        return jdbc.queryForObject("SELECT id, status FROM reservation WHERE id = ?", (rs, row) ->
                new Reservation(rs.getString("id"), rs.getString("status")), id);
    }
}
