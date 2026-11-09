CREATE TABLE IF NOT EXISTS reservation (
    id VARCHAR(36) PRIMARY KEY,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS import_operation (
    id VARCHAR(36) PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    imported_count INTEGER NOT NULL,
    object_key VARCHAR(100),
    file_name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS imported_visit (
    import_id VARCHAR(36) NOT NULL,
    position INTEGER NOT NULL,
    pet_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (import_id, position)
);
