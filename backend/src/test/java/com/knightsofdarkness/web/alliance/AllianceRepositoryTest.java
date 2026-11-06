package com.knightsofdarkness.web.alliance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;

import com.knightsofdarkness.web.GameServerTest;
import com.knightsofdarkness.web.alliance.model.AllianceEntity;

@GameServerTest
class AllianceRepositoryTest {
    @Autowired
    private IAllianceRepository alliances;

    @Autowired
    private TransactionTemplate transactions;

    @Test
    void aSecondAllianceOfOneName_breaksTheKeyInsteadOfReplacingTheFirst()
    {
        transactions.executeWithoutResult(status -> alliances.create(new AllianceEntity("Twin Banners", "uprzejmy")));

        assertThrows(DataIntegrityViolationException.class,
                () -> transactions.executeWithoutResult(status -> alliances.create(new AllianceEntity("Twin Banners", "Umbar"))));

        assertEquals("uprzejmy", alliances.getAllianceByName("Twin Banners").orElseThrow().getEmperor());
    }
}
