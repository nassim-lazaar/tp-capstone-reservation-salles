package com.example.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDateTime dateFin;

    @Column(name = "motif", length = 200)
    private String motif;

    @Column(name = "statut", nullable = false, length = 30)
    private String statut = "CONFIRMEE";

    @Version
    @Column(name = "version_opt")
    private int version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    public Reservation() {
    }

    public Reservation(Salle s, Utilisateur u, LocalDateTime debut, LocalDateTime fin, String motif) {
        this.salle = s;
        this.utilisateur = u;
        this.dateDebut = debut;
        this.dateFin = fin;
        this.motif = motif;
        this.statut = "CONFIRMEE";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long idRes) {
        this.id = idRes;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime debut) {
        this.dateDebut = debut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime fin) {
        this.dateFin = fin;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String raison) {
        this.motif = raison;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String etat) {
        this.statut = etat;
    }

    public int getVersion() {
        return version;
    }

    public Salle getSalle() {
        return salle;
    }

    public void setSalle(Salle s) {
        this.salle = s;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur u) {
        this.utilisateur = u;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", motif='" + motif + '\'' +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", statut='" + statut + '\'' +
                ", version=" + version +
                '}';
    }
}
