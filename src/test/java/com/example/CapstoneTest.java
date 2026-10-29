package com.example;

import com.example.migration.DatabaseMigrationTool;
import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.service.ReservationService;
import com.example.util.DataInitializer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

public class CapstoneTest {

    private static EntityManagerFactory emf;

    @BeforeClass
    public static void setUp() {
        emf = Persistence.createEntityManagerFactory("ReservationPU");
        EntityManager em = emf.createEntityManager();
        DatabaseMigrationTool.executeMigration(em, "db/migration/v2_0_add_description.sql");
        DataInitializer.initialize(em);
        em.close();
    }

    @AfterClass
    public static void tearDown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Test
    public void testAvailableRooms() {
        ReservationService service = new ReservationService(emf);
        LocalDateTime now = LocalDateTime.now();
        List<Salle> disponibles = service.trouverSallesDisponibles(now.plusDays(10), now.plusDays(10).plusHours(2));
        assertFalse(disponibles.isEmpty());
        assertTrue(disponibles.size() >= 3);
    }

    @Test
    public void testOptimisticLockingAndRetry() {
        ReservationService service = new ReservationService(emf);
        EntityManager em = emf.createEntityManager();

        Reservation res = em.createQuery("SELECT r FROM Reservation r", Reservation.class)
                .setMaxResults(1)
                .getSingleResult();
        Long id = res.getId();
        em.close();

        // 2 separate copies with same version
        EntityManager em1 = emf.createEntityManager();
        Reservation r1 = em1.find(Reservation.class, id);

        EntityManager em2 = emf.createEntityManager();
        Reservation r2 = em2.find(Reservation.class, id);

        // Update 1
        r1.setMotif("Test Update 1");
        service.modifierReservation(r1);
        em1.close();

        // Update 2 with retry
        r2.setMotif("Test Update 2 Resolved");
        Reservation result = service.modifierAvecRetry(r2, 3);
        assertNotNull(result);
        assertEquals("Test Update 2 Resolved", result.getMotif());
        em2.close();
    }
}
