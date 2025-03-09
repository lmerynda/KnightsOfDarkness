package com.knightsofdarkness.web.alliance.model;

import java.util.ArrayList;
import java.util.List;

import com.knightsofdarkness.common.alliance.AllianceDto;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class AllianceEntity {

    @Id
    @Column(name = "name", nullable = false, unique = true)
    String name;

    @OneToMany(mappedBy = "alliance", cascade = CascadeType.ALL)
    List<KingdomEntity> kingdoms;

    @Column(name = "emperor", nullable = false)
    String emperor;

    public AllianceEntity()
    {
    }

    public AllianceEntity(String name, List<KingdomEntity> kingdoms, String emperor)
    {
        this.name = name;
        this.kingdoms = kingdoms;
        this.emperor = emperor;
    }

    public AllianceEntity(String name, String emperor)
    {
        this.name = name;
        this.kingdoms = new ArrayList<>();
        this.emperor = emperor;
    }

    public AllianceDto toDto()
    {
        return new AllianceDto(name, emperor);
    }

    public String getName()
    {
        return name;
    }

    public String getEmperor()
    {
        return emperor;
    }

    public void addKingdom(KingdomEntity kingdom)
    {
        kingdoms.add(kingdom);
        kingdom.setAlliance(this);
    }

    public List<KingdomEntity> getKingdoms()
    {
        return kingdoms;
    }
}
