package ru.liqbez.appointment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import org.postgresql.xa.PGXADataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class XaTwoPhaseCommitService {
    private final String patientUrl;
    private final String billingUrl;
    private final String user;
    private final String password;

    public XaTwoPhaseCommitService(@Value("${demo.xa.patient-url}") String patientUrl,
                        @Value("${demo.xa.billing-url}") String billingUrl,
                        @Value("${demo.xa.user}") String user,
                        @Value("${demo.xa.password}") String password) {
        this.patientUrl = patientUrl;
        this.billingUrl = billingUrl;
        this.user = user;
        this.password = password;
    }

    public XaResult run(UUID id, boolean failBeforePrepare) throws Exception {
        PGXADataSource patient = source(patientUrl);
        PGXADataSource billing = source(billingUrl);
        XAConnection first = patient.getXAConnection();
        XAConnection second;
        try {
            second = billing.getXAConnection();
        } catch (Exception ex) {
            first.close();
            throw ex;
        }
        try (Connection firstSql = first.getConnection(); Connection secondSql = second.getConnection()) {
            XAResource firstResource = first.getXAResource();
            XAResource secondResource = second.getXAResource();
            Xid firstXid = new DemoXid(id, 1);
            Xid secondXid = new DemoXid(id, 2);
            boolean prepared = false;
            firstResource.start(firstXid, XAResource.TMNOFLAGS);
            secondResource.start(secondXid, XAResource.TMNOFLAGS);
            try {
                insert(firstSql, "xa_patient_audit", id);
                insert(secondSql, "xa_billing_audit", id);
                firstResource.end(firstXid, XAResource.TMSUCCESS);
                secondResource.end(secondXid, XAResource.TMSUCCESS);
                if (failBeforePrepare) throw new IllegalStateException("Injected failure before XA prepare");
                firstResource.prepare(firstXid);
                secondResource.prepare(secondXid);
                prepared = true;
                firstResource.commit(firstXid, false);
                secondResource.commit(secondXid, false);
                return new XaResult(id, "COMMITTED", true, true);
            } catch (Exception ex) {
                if (!prepared) {
                    try { firstResource.rollback(firstXid); } catch (Exception ignored) { }
                    try { secondResource.rollback(secondXid); } catch (Exception ignored) { }
                }
                throw ex;
            }
        } finally {
            first.close();
            second.close();
        }
    }

    public XaResult find(UUID id) throws Exception {
        return new XaResult(id, "OBSERVED", exists(patientUrl, "xa_patient_audit", id),
                exists(billingUrl, "xa_billing_audit", id));
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
                "INSERT INTO " + table + " (id, note) VALUES (?, ?)") ) {
            statement.setObject(1, id);
            statement.setString(2, "pet clinic XA demo");
            statement.executeUpdate();
        }
    }

    private boolean exists(String url, String table, UUID id) throws Exception {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM " + table + " WHERE id = ?")) {
            statement.setObject(1, id);
            try (ResultSet result = statement.executeQuery()) { return result.next(); }
        }
    }

    public record XaResult(UUID id, String status, boolean patientDatabase, boolean billingDatabase) {}

    private record DemoXid(byte[] global, byte[] branch) implements Xid {
        DemoXid(UUID id, int branch) {
            this((id.toString()).getBytes(java.nio.charset.StandardCharsets.UTF_8), new byte[]{(byte) branch});
        }
        @Override public int getFormatId() { return 0x4C495142; }
        @Override public byte[] getGlobalTransactionId() { return global; }
        @Override public byte[] getBranchQualifier() { return branch; }
    }
}
