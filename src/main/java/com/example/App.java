package com.example;

import com.example.migration.DatabaseMigrationTool;
import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.model.Utilisateur;
import com.example.service.ReservationService;
import com.example.util.DataInitializer;
import com.example.util.PerformanceReport;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class App {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("   TP CAPSTONE : FINALISATION APPLICATION 'RESERVATION DE SALLES'      ");
        System.out.println("   JPA 2.2 - HIBERNATE 5.6 - EHCACHE L2 - MIGRATION SQL - CONCURRENCE   ");
        System.out.println("========================================================================");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ReservationPU");
        EntityManager em = emf.createEntityManager();

        try {
            // -----------------------------------------------------------------
            // MODULE 1 : EXECUTION DE LA MIGRATION DE BASE DE DONNEES
            // -----------------------------------------------------------------
            System.out.println("\n[MODULE 1] OUTIL DE MIGRATION DE BASE DE DONNEES (DatabaseMigrationTool)");
            System.out.println("------------------------------------------------------------------------");
            DatabaseMigrationTool.executeMigration(em, "db/migration/v2_0_add_description.sql");

            // -----------------------------------------------------------------
            // MODULE 2 : INITIALISATION DU JEU DE DONNEES METIER
            // -----------------------------------------------------------------
            System.out.println("\n[MODULE 2] PEUPLEMENT INITIAL DES ENTITES METIER (DataInitializer)");
            System.out.println("------------------------------------------------------------------------");
            DataInitializer.initialize(em);

            ReservationService resService = new ReservationService(emf);

            // -----------------------------------------------------------------
            // MODULE 3 : CONSULTATION ET RECHERCHE AVANCEE (JPQL & CACHE)
            // -----------------------------------------------------------------
            System.out.println("\n[MODULE 3] RECHERCHE DE DISPONIBILITE & ASSOCIATIONS");
            System.out.println("------------------------------------------------------------------------");
            LocalDateTime slotDebut = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            LocalDateTime slotFin = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0);
            System.out.println("Recherche des salles libres pour le creneau : " + slotDebut + " -> " + slotFin);

            List<Salle> disponibles = resService.trouverSallesDisponibles(slotDebut, slotFin);
            System.out.println("Salles disponibles trouvees (" + disponibles.size() + ") :");
            for (Salle s : disponibles) {
                System.out.println("  -> [SALLE] " + s.getNom() + " | Capacite=" + s.getCapacite() + " | Desc: " + s.getDescription());
                System.out.print("     Equipements : ");
                s.getEquipements().forEach(eq -> System.out.print("[" + eq.getNom() + "] "));
                System.out.println();
            }

            // -----------------------------------------------------------------
            // MODULE 4 : CONCURRENCE MULTI-THREADS & VERROUILLAGE OPTIMISTE
            // -----------------------------------------------------------------
            System.out.println("\n[MODULE 4] SIMULATION DE CONCURRENCE AVEC COUNTDOWNLATCH & RETRY");
            System.out.println("------------------------------------------------------------------------");

            Reservation targetRes = em.createQuery("SELECT r FROM Reservation r ORDER BY r.id", Reservation.class)
                    .setMaxResults(1)
                    .getSingleResult();
            Long resId = targetRes.getId();
            System.out.println("Cible de test concurrence : Reservation ID=" + resId + " (Version initiale: " + targetRes.getVersion() + ")");

            CountDownLatch latch = new CountDownLatch(1);
            CountDownLatch finishLatch = new CountDownLatch(2);

            // Thread 1
            new Thread(() -> {
                try {
                    latch.await();
                    EntityManager emThread = emf.createEntityManager();
                    try {
                        Reservation r = emThread.find(Reservation.class, resId);
                        r.setMotif("Modification par Thread 1 (Prioritaire)");
                        resService.modifierReservation(r);
                        System.out.println("  [Thread 1] Commit reussi ! Version en base mise a jour.");
                    } finally {
                        emThread.close();
                    }
                } catch (Exception e) {
                    System.out.println("  [Thread 1] Erreur : " + e.getMessage());
                } finally {
                    finishLatch.countDown();
                }
            }).start();

            // Thread 2 (Simule conflit et retry)
            new Thread(() -> {
                try {
                    latch.await();
                    Thread.sleep(20); // Laisser Thread 1 committer en premier pour provoquer le conflit
                    EntityManager emThread = emf.createEntityManager();
                    try {
                        Reservation r = emThread.find(Reservation.class, resId);
                        r.setMotif("Modification par Thread 2 (Avec Retry)");
                        Reservation resolue = resService.modifierAvecRetry(r, 3);
                        System.out.println("  [Thread 2] Succes final apres retry ! Version finale=" + resolue.getVersion());
                    } finally {
                        emThread.close();
                    }
                } catch (Exception e) {
                    System.out.println("  [Thread 2] Exception : " + e.getMessage());
                } finally {
                    finishLatch.countDown();
                }
            }).start();

            // Declenchement simultane
            latch.countDown();
            try {
                finishLatch.await();
            } catch (InterruptedException ignored) {}

            em.clear();
            Reservation finalCheck = em.find(Reservation.class, resId);
            System.out.println("\nEtat final de la reservation en base :");
            System.out.println("  -> ID=" + finalCheck.getId() + " | Motif='" + finalCheck.getMotif() + "' | Version=" + finalCheck.getVersion());

            // -----------------------------------------------------------------
            // MODULE 5 : RAPPORT DE PERFORMANCE ET VALIDATION DU CACHE L2
            // -----------------------------------------------------------------
            PerformanceReport.printReport(em);

            System.out.println("\n========================================================================");
            System.out.println("   PROJET CAPSTONE TERMINE AVEC SUCCES : TOUTES EXIGENCES VALIDEES      ");
            System.out.println("========================================================================");

        } finally {
            if (em.isOpen()) em.close();
            if (emf.isOpen()) emf.close();
        }
    }
}
