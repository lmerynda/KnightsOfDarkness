package com.knightsofdarkness.storage.alliance;

import java.util.List;
import java.util.stream.Collectors;

import com.knightsofdarkness.common.alliance.AllianceDto;
import com.knightsofdarkness.game.alliance.Alliance;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.storage.kingdom.KingdomEntity;

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
        var kingdoms = this.kingdoms.stream().map(kingdom -> kingdom.toDomainModel(gameConfig)).collect(Collectors.toList());
        return new Alliance(name, kingdoms, emperor);
    }

    public static AllianceEntity fromDomainModel(Alliance alliance)
    {
        var kingdoms = alliance.getKingdoms().stream().map(KingdomEntity::fromDomainModel).collect(Collectors.toList());
        return new AllianceEntity(alliance.getName(), kingdoms, alliance.getEmperor());
    }

    public static AllianceEntity fromDto(AllianceDto alliance, String emperor)
    {
        return new AllianceEntity(alliance.name(), List.of(), emperor);
    }

    public static AllianceEntity fromDto(String name, String emperor)
    {
        return new AllianceEntity(name, List.of(), emperor);
    }

    public String getName()
    {
        return name;
    }
}
