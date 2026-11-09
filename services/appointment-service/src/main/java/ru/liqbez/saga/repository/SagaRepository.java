package ru.liqbez.saga.repository;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import ru.liqbez.saga.model.SagaResult;

@Repository
public class SagaRepository {
    private final ConcurrentHashMap<String, SagaResult> results = new ConcurrentHashMap<>();

    public SagaResult find(String id) {
        return results.get(id);
    }

    public void save(SagaResult result) {
        results.put(result.id(), result);
    }
}
