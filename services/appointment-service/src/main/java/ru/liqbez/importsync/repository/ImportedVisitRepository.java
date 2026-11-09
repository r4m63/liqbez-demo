package ru.liqbez.importsync.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ImportedVisitRepository {
    private final JdbcTemplate jdbc;

    public ImportedVisitRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void save(UUID importId, List<String> petNames) {
        for (int index = 0; index < petNames.size(); index++) {
            jdbc.update("INSERT INTO imported_visit (import_id, position, pet_name) VALUES (?, ?, ?)",
                    importId.toString(), index, petNames.get(index));
        }
    }

    public List<String> findNames(UUID importId) {
        return jdbc.query("SELECT pet_name FROM imported_visit WHERE import_id = ? ORDER BY position",
                (rs, row) -> rs.getString("pet_name"), importId.toString());
    }
}
