package com.example.service;

import com.example.model.Reservation;
import com.example.model.Salle;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.OptimisticLockException;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationService {

    private final EntityManagerFactory emf;

    public ReservationService(EntityManagerFactory factory) {
        this.emf = factory;
    }

    public List<Salle> trouverSallesDisponibles(LocalDateTime debut, LocalDateTime fin) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT s FROM Salle s WHERE s.id NOT IN (" +
                          "SELECT r.salle.id FROM Reservation r WHERE r.statut = 'CONFIRMEE' AND (:debut < r.dateFin AND :fin > r.dateDebut)) " +
                          "ORDER BY s.id";
            return em.createQuery(jpql, Salle.class)
                    .setParameter("debut", debut)
                    .setParameter("fin", fin)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Reservation modifierReservation(Reservation reservation) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Reservation merged = em.merge(reservation);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            Throwable cause = e;
            while (cause != null) {
                if (cause instanceof OptimisticLockException) {
                    throw (OptimisticLockException) cause;
                }
                if (cause instanceof org.hibernate.StaleObjectStateException) {
                    throw new OptimisticLockException(cause.getMessage(), cause);
                }
                cause = cause.getCause();
            }
            throw new RuntimeException("Erreur lors de la modification", e);
        } finally {
            em.close();
        }
    }

    public Reservation modifierAvecRetry(Reservation reservation, int maxRetries) {
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                return modifierReservation(reservation);
            } catch (OptimisticLockException ole) {
                attempt++;
                System.out.println("  [RETRY] Conflit de version detecte ! Tentative de rejeu " + attempt + "/" + maxRetries);
                try {
                    Thread.sleep(50 * attempt);
                } catch (InterruptedException ignored) {}

                EntityManager em = emf.createEntityManager();
                try {
                    Reservation fresh = em.find(Reservation.class, reservation.getId());
                    if (fresh != null) {
                        fresh.setMotif(reservation.getMotif());
                        fresh.setStatut(reservation.getStatut());
                        fresh.setDateDebut(reservation.getDateDebut());
                        fresh.setDateFin(reservation.getDateFin());
                        reservation = fresh;
                    }
                } finally {
                    em.close();
                }
            }
        }
        throw new OptimisticLockException("Echec de la mise a jour apres " + maxRetries + " tentatives.");
    }
}
