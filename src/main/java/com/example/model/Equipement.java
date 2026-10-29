package com.example.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipements")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Equipement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_equipement", nullable = false)
    private String nom;

    @ManyToMany(mappedBy = "equipements", fetch = FetchType.LAZY)
    private List<Salle> salles = new ArrayList<>();

    public Equipement() {
    }

    public Equipement(String nomMateriel) {
        this.nom = nomMateriel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long idEquip) {
        this.id = idEquip;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nomMateriel) {
        this.nom = nomMateriel;
    }

    public List<Salle> getSalles() {
        return salles;
    }

    public void setSalles(List<Salle> listeSalles) {
        this.salles = listeSalles;
    }

    @Override
    public String toString() {
        return nom;
    }
}
