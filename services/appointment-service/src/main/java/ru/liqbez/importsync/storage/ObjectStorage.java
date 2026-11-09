package ru.liqbez.importsync.storage;

public interface ObjectStorage {
    void put(String key, byte[] content);
    void remove(String key);
    boolean exists(String key);
}
