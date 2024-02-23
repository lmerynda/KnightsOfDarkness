package com.example.knightsofdarknessstorage.Kingdom;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "kingdoms")
public class KingdomEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    // public Kingdom toDomainModel()
    // {
    // Kingdom kingdom = new Kingdom();
    // kingdom.setId(this.id);
    // kingdom.setName(this.name);
    // return kingdom;
    // }
}
