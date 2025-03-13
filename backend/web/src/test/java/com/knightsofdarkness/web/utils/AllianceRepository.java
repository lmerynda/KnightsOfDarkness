package com.knightsofdarkness.web.utils;

import java.util.Optional;

import java.util.ArrayList;
import java.util.List;

import com.knightsofdarkness.web.alliance.IAllianceRepository;
import com.knightsofdarkness.web.alliance.model.AllianceEntity;
import com.knightsofdarkness.web.alliance.model.AllianceInvitationEntity;

public class AllianceRepository implements IAllianceRepository {
    List<AllianceEntity> alliances = new ArrayList<>();
    List<AllianceInvitationEntity> invitations = new ArrayList<>();

    @Override
    public Optional<AllianceEntity> getAllianceByName(String name)
    {
        return alliances.stream().filter(alliance -> alliance.getName().equals(name)).findFirst();
    }

    @Override
    public void create(AllianceEntity alliance)
    {
        alliances.add(alliance);
    }

    @Override
    public void update(AllianceEntity alliance)
    {
        for (var savedAlliance : alliances)
        {
            if (savedAlliance.getName().equals(alliance.getName()))
            {
                alliances.remove(savedAlliance);
                alliances.add(alliance);

                return;
            }
        }

        throw new IllegalArgumentException("Alliance not found");
    }

    @Override
    public List<AllianceEntity> getAlliances()
    {
        return alliances;
    }

    @Override
    public AllianceInvitationEntity createInvitation(AllianceInvitationEntity invitation)
    {
        // TODO check if exists? maybe it should be a map?
        invitations.add(invitation);
        return invitation;
    }
}
