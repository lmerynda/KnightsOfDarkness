package com.knightsofdarkness.web.alliance.model;

import java.util.List;
import java.util.stream.Collectors;

import com.knightsofdarkness.common.alliance.CreateAllianceDto;
import com.knightsofdarkness.game.alliance.Alliance;
import com.knightsofdarkness.game.gameconfig.GameConfig;
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

    public Alliance toDomainModel(GameConfig gameConfig)
    {
        // TODO rework join kingdoms here again
        return new Alliance(name, List.of(), emperor);
    }

    public static AllianceEntity fromDomainModel(Alliance alliance)
    {
        var kingdoms = alliance.getKingdoms().stream().map(KingdomEntity::fromDomainModel).collect(Collectors.toList());
        return new AllianceEntity(alliance.getName(), kingdoms, alliance.getEmperor());
    }

    public static AllianceEntity fromDto(CreateAllianceDto alliance, String emperor)
    {
        return new AllianceEntity(alliance.name(), List.of(), emperor);
    }
}
