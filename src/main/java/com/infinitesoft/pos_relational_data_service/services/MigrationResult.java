package com.infinitesoft.pos_relational_data_service.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Result DTO for migration operations.
 */
public class MigrationResult {
    private int created;
    private int skipped;
    private int duplicates;
    private int errors;
    private List<String> messages;
    private List<Conflict> conflictos;
    private boolean fatalError;

    public MigrationResult() {}

    public MigrationResult(int created, int skipped, int duplicates, int errors, List<String> messages) {
        this.created = created;
        this.skipped = skipped;
        this.duplicates = duplicates;
        this.errors = errors;
        this.messages = messages;
        this.conflictos = new ArrayList<>();
    }

    public MigrationResult(int created, int skipped, int duplicates, int errors, List<String> messages, List<Conflict> conflictos) {
        this.created = created;
        this.skipped = skipped;
        this.duplicates = duplicates;
        this.errors = errors;
        this.messages = messages;
        this.conflictos = conflictos;
    }

    public static MigrationResult error(String msg) {
        MigrationResult r = new MigrationResult(0, 0, 0, 1, Collections.singletonList(msg));
        r.fatalError = true;
        return r;
    }

    public boolean isFatalError() { return fatalError; }

    public int getCreated() { return created; }
    public int getSkipped() { return skipped; }
    public int getDuplicates() { return duplicates; }
    public int getErrors() { return errors; }
    public List<String> getMessages() { return messages; }
    public List<Conflict> getConflictos() { return conflictos; }

    public void incCreated() { this.created++; }
    public void incSkipped() { this.skipped++; }
    public void incDuplicates() { this.duplicates++; }
    public void incErrors() { this.errors++; }
    public void addMessage(String m) { if (this.messages != null) this.messages.add(m); }
    public void addConflict(Conflict c) { 
        if (this.conflictos == null) this.conflictos = new ArrayList<>();
        this.conflictos.add(c); 
    }

    public static class Conflict {
        private String referencia;
        private String nombre;
        private Object conflicto;

        public Conflict() {}

        public Conflict(String referencia, Object conflicto) {
            this.referencia = referencia;
            this.conflicto = conflicto;
        }

        public Conflict(String referencia, String nombre, Object conflicto) {
            this.referencia = referencia;
            this.nombre = nombre;
            this.conflicto = conflicto;
        }

        public String getReferencia() { return referencia; }
        public void setReferencia(String referencia) { this.referencia = referencia; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public Object getConflicto() { return conflicto; }
        public void setConflicto(Object conflicto) { this.conflicto = conflicto; }
    }
}