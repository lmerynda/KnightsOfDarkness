package com.knightsofdarkness.game.alliance;

import java.util.List;

import com.knightsofdarkness.game.kingdom.Kingdom;

public class Alliance {
    private final String name;
    private final List<Kingdom> kingdoms;
    private final String emperor;

    public Alliance(String name, List<Kingdom> kingdoms, String emperor)
    {
        this.name = name;
        this.kingdoms = kingdoms;
        this.emperor = emperor;
    }

    public String getName()
    {
        return name;
    }

    public List<Kingdom> getKingdoms()
    {
        return kingdoms;
    }

    public String getEmperor()
    {
        return emperor;
    }

    public void addKingdom(Kingdom kingdom)
    {
        kingdoms.add(kingdom);
    }
}
