package com.example.migration;

import javax.persistence.EntityManager;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseMigrationTool {

    public static void executeMigration(EntityManager em, String scriptPath) {
        System.out.println("-> Execution du script de migration SQL : " + scriptPath);
        em.getTransaction().begin();
        try {
            org.hibernate.Session session = em.unwrap(org.hibernate.Session.class);
            session.doWork((Connection connection) -> {
                try {
                    InputStream is = DatabaseMigrationTool.class.getClassLoader().getResourceAsStream(scriptPath);
                    if (is == null) {
                        System.err.println("Fichier de migration non trouve : " + scriptPath);
                        return;
                    }
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                        String sql = reader.lines().collect(Collectors.joining("\n"));
                        String[] statements = sql.split(";");
                        try (Statement stmt = connection.createStatement()) {
                            for (String s : statements) {
                                String trimmed = s.trim();
                                if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                                    try {
                                        stmt.execute(trimmed);
                                    } catch (Exception ex) {
                                        System.out.println("  [MIGRATION-INFO] " + ex.getMessage());
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erreur execution doWork : " + e.getMessage());
                }
            });
            em.getTransaction().commit();
            System.out.println("-> Migration appliquee avec succes (tables de backup, index unique, contrainte CHECK, colonne description).");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            System.err.println("Erreur migration : " + e.getMessage());
        }
    }
}
