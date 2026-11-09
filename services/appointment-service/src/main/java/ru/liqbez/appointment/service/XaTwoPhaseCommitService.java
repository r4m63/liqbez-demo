package ru.liqbez.appointment.service;

import java.sql.Connection;
import java.util.UUID;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import org.springframework.stereotype.Service;
import ru.liqbez.appointment.model.XaResult;
import ru.liqbez.appointment.repository.XaAuditRepository;

@Service
public class XaTwoPhaseCommitService {
    private final XaAuditRepository repository;

    public XaTwoPhaseCommitService(XaAuditRepository repository) {
        this.repository = repository;
    }

    public XaResult run(UUID id, boolean failBeforePrepare) throws Exception {
        XAConnection first = repository.openPatient();
        XAConnection second;
        try {
            second = repository.openBilling();
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
                repository.insertPatient(firstSql, id);
                repository.insertBilling(secondSql, id);
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
        return new XaResult(id, "OBSERVED", repository.patientExists(id), repository.billingExists(id));
    }

    private record DemoXid(byte[] global, byte[] branch) implements Xid {
        DemoXid(UUID id, int branch) {
            this((id.toString()).getBytes(java.nio.charset.StandardCharsets.UTF_8), new byte[]{(byte) branch});
        }
        @Override public int getFormatId() { return 0x4C495142; }
        @Override public byte[] getGlobalTransactionId() { return global; }
        @Override public byte[] getBranchQualifier() { return branch; }
    }
}
