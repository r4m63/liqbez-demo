package ru.liqbez.appointment.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import javax.sql.XAConnection;
import org.postgresql.xa.PGXADataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class XaAuditRepository {
    private final String patientUrl;
    private final String billingUrl;
    private final String user;
    private final String password;

    public XaAuditRepository(@Value("${demo.xa.patient-url}") String patientUrl,
                             @Value("${demo.xa.billing-url}") String billingUrl,
                             @Value("${demo.xa.user}") String user,
                             @Value("${demo.xa.password}") String password) {
        this.patientUrl = patientUrl;
        this.billingUrl = billingUrl;
        this.user = user;
        this.password = password;
    }

    public XAConnection openPatient() throws Exception {
        return source(patientUrl).getXAConnection();
    }

    public XAConnection openBilling() throws Exception {
        return source(billingUrl).getXAConnection();
    }

    public void insertPatient(Connection connection, UUID id) throws Exception {
        insert(connection, "xa_patient_audit", id);
    }

    public void insertBilling(Connection connection, UUID id) throws Exception {
        insert(connection, "xa_billing_audit", id);
    }

    public boolean patientExists(UUID id) throws Exception {
        return exists(patientUrl, "xa_patient_audit", id);
    }

    public boolean billingExists(UUID id) throws Exception {
        return exists(billingUrl, "xa_billing_audit", id);
    }

    private PGXADataSource source(String url) {
        PGXADataSource source = new PGXADataSource();
        source.setUrl(url);
        source.setUser(user);
        source.setPassword(password);
        return source;
    }

    private void insert(Connection connection, String table, UUID id) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO " + table + " (id, note) VALUES (?, ?)")) {
            statement.setObject(1, id);
            statement.setString(2, "pet clinic XA demo");
            statement.executeUpdate();
        }
    }

    private boolean exists(String url, String table, UUID id) throws Exception {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM " + table + " WHERE id = ?")) {
            statement.setObject(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }
}
