package ru.liqbez.payment.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.liqbez.payment.model.Payment;

@Repository
public class PaymentRepository {
    private final JdbcTemplate jdbc;

    public PaymentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void charge(String id) {
        jdbc.update("MERGE INTO payment (id, status) KEY (id) VALUES (?, 'CHARGED')", id);
    }

    public Payment find(String id) {
        return jdbc.queryForObject("SELECT id, status FROM payment WHERE id = ?", (rs, row) ->
                new Payment(rs.getString("id"), rs.getString("status")), id);
    }
}
