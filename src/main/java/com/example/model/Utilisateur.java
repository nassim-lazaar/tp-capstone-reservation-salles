package com.example.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "utilisateurs")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_user", nullable = false)
    private String nom;

    @Column(name = "prenom_user")
    private String prenom;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Version
    @Column(name = "version_opt")
    private int version;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservation> reservations = new ArrayList<>();

    public Utilisateur() {
    }

    public Utilisateur(String nomUser, String prenomUser, String adresseEmail) {
        this.nom = nomUser;
        this.prenom = prenomUser;
        this.email = adresseEmail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long idUser) {
        this.id = idUser;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nomUser) {
        this.nom = nomUser;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenomUser) {
        this.prenom = prenomUser;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String adresseEmail) {
        this.email = adresseEmail;
    }

    public int getVersion() {
        return version;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> listeReservations) {
        this.reservations = listeReservations;
    }

    @Override
    public String toString() {
        return nom + " " + (prenom != null ? prenom : "") + " (" + email + ")";
    }
}
