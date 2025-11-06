package com.infinitesoft.pos_relational_data_service.services;

import java.util.Collections;
import java.util.List;

/**
 * Result DTO for migration operations.
 */
public class MigrationResult {
    private int created;
    private int skipped;
    private int duplicates;
    private int errors;
    private List<String> messages;

    public MigrationResult() {}

    public MigrationResult(int created, int skipped, int duplicates, int errors, List<String> messages) {
        this.created = created;
        this.skipped = skipped;
        this.duplicates = duplicates;
        this.errors = errors;
        this.messages = messages;
    }

    public static MigrationResult error(String msg) {
        return new MigrationResult(0, 0, 0, 1, Collections.singletonList(msg));
    }

    public int getCreated() { return created; }
    public int getSkipped() { return skipped; }
    public int getDuplicates() { return duplicates; }
    public int getErrors() { return errors; }
    public List<String> getMessages() { return messages; }

    public void incCreated() { this.created++; }
    public void incSkipped() { this.skipped++; }
    public void incDuplicates() { this.duplicates++; }
    public void incErrors() { this.errors++; }
    public void addMessage(String m) { if (this.messages != null) this.messages.add(m); }
}