package com.example.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "salles")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Salle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_salle", nullable = false)
    private String nom;

    @Column(name = "capacite_accueil", nullable = false)
    private int capacite;

    @Column(name = "description", length = 255)
    private String description;

    @OneToMany(mappedBy = "salle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Reservation> reservations = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "salle_equipement",
            joinColumns = @JoinColumn(name = "salle_id"),
            inverseJoinColumns = @JoinColumn(name = "equipement_id")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<Equipement> equipements = new ArrayList<>();

    public Salle() {
    }

    public Salle(String nomSalle, int capaciteAccueil, String desc) {
        this.nom = nomSalle;
        this.capacite = capaciteAccueil;
        this.description = desc;
    }

    public void addEquipement(Equipement e) {
        if (e != null && !equipements.contains(e)) {
            equipements.add(e);
            e.getSalles().add(this);
        }
    }

    public void removeEquipement(Equipement e) {
        if (e != null) {
            equipements.remove(e);
            e.getSalles().remove(this);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long idSalle) {
        this.id = idSalle;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nomSalle) {
        this.nom = nomSalle;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capaciteAccueil) {
        this.capacite = capaciteAccueil;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> listeReservations) {
        this.reservations = listeReservations;
    }

    public List<Equipement> getEquipements() {
        return equipements;
    }

    public void setEquipements(List<Equipement> listeEquipements) {
        this.equipements = listeEquipements;
    }

    @Override
    public String toString() {
        return nom + " (Capacite: " + capacite + " places)";
    }
}
