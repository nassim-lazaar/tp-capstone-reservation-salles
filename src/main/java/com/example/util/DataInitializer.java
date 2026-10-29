package com.example.util;

import com.example.model.Equipement;
import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.model.Utilisateur;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;

public class DataInitializer {

    public static void initialize(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 1. Equipements
        Equipement eqVisio = new Equipement("Systeme Visioconference Polycom");
        Equipement eqProjecteur = new Equipement("Videoprojecteur Laser 4K");
        Equipement eqTableau = new Equipement("Tableau Blanc Interactif");
        Equipement eqSon = new Equipement("Systeme Audio & Micros sans fil");

        em.persist(eqVisio);
        em.persist(eqProjecteur);
        em.persist(eqTableau);
        em.persist(eqSon);

        // 2. Salles
        Salle s1 = new Salle("Salle Turing", 25, "Salle de reunion connectee et insonorisee");
        s1.addEquipement(eqVisio);
        s1.addEquipement(eqProjecteur);

        Salle s2 = new Salle("Salle Lovelace", 50, "Salle de formation et conference modulaire");
        s2.addEquipement(eqProjecteur);
        s2.addEquipement(eqTableau);
        s2.addEquipement(eqSon);

        Salle s3 = new Salle("Amphi Dijkstra", 150, "Grand amphitheatre pour assemblees et keynotes");
        s3.addEquipement(eqVisio);
        s3.addEquipement(eqProjecteur);
        s3.addEquipement(eqSon);

        em.persist(s1);
        em.persist(s2);
        em.persist(s3);

        // 3. Utilisateurs
        Utilisateur u1 = new Utilisateur("Lazaar", "Nassim", "nassim.lazaar@example.com");
        Utilisateur u2 = new Utilisateur("Idrissi", "Karim", "karim.idrissi@example.com");
        Utilisateur u3 = new Utilisateur("Benali", "Sarah", "sarah.benali@example.com");

        em.persist(u1);
        em.persist(u2);
        em.persist(u3);

        // 4. Reservations
        LocalDateTime now = LocalDateTime.now();
        Reservation r1 = new Reservation(s1, u1, now.plusDays(1).withHour(9).withMinute(0), now.plusDays(1).withHour(11).withMinute(0), "Point Technique Hebdo");
        Reservation r2 = new Reservation(s2, u2, now.plusDays(1).withHour(14).withMinute(0), now.plusDays(1).withHour(17).withMinute(0), "Atelier Formation JPA");
        Reservation r3 = new Reservation(s3, u3, now.plusDays(2).withHour(10).withMinute(0), now.plusDays(2).withHour(12).withMinute(0), "Reunion Trimestrielle");

        em.persist(r1);
        em.persist(r2);
        em.persist(r3);

        tx.commit();
        em.clear();
        System.out.println("-> Donnees initialisees avec succes : 4 equipements, 3 salles, 3 utilisateurs, 3 reservations.");
    }
}
