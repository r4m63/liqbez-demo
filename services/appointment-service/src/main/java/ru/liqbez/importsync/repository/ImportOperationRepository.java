package ru.liqbez.importsync.repository;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.liqbez.importsync.model.ImportOperation;

@Repository
public class ImportOperationRepository {
    private final JdbcTemplate jdbc;

    public ImportOperationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void create(UUID id, String fileName) {
        jdbc.update("INSERT INTO import_operation (id, status, imported_count, object_key, file_name) "
                + "VALUES (?, 'STARTED', 0, NULL, ?)", id.toString(), fileName);
    }

    public void complete(UUID id, int count, String objectKey) {
        jdbc.update("UPDATE import_operation SET status = 'COMPLETED', imported_count = ?, object_key = ? WHERE id = ?",
                count, objectKey, id.toString());
    }

    public void fail(UUID id, String status) {
        jdbc.update("UPDATE import_operation SET status = ? WHERE id = ?", status, id.toString());
    }

    public ImportOperation find(UUID id) {
        return jdbc.queryForObject("SELECT id, status, imported_count, object_key, file_name "
                        + "FROM import_operation WHERE id = ?", (rs, row) -> new ImportOperation(
                UUID.fromString(rs.getString("id")), rs.getString("status"),
                rs.getInt("imported_count"), rs.getString("object_key"), rs.getString("file_name")), id.toString());
    }
}
