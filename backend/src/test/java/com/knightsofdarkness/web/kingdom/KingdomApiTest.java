package com.knightsofdarkness.web.kingdom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.JsonPath;
import com.knightsofdarkness.web.GameServerTest;
import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.model.KingdomRepository;
import com.knightsofdarkness.web.round.RoundService;

/**
 * Requests as the browser sends them: unit, building and resource maps are flat objects keyed by name.
 */
@GameServerTest
class KingdomApiTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KingdomService kingdomService;

    @Autowired
    private KingdomRepository kingdomRepository;

    @Autowired
    private GameConfig gameConfig;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private RoundService roundService;

    private String kingdomName;

    private String token;

    @BeforeEach
    void logIn() throws Exception
    {
        var email = "api-" + System.nanoTime() + "@example.com";
        kingdomName = "Api " + (System.nanoTime() % 1000000);
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"kingdomName\":\"" + kingdomName + "\",\"password\":\"long-enough-password\"}"))
                .andExpect(status().isOk());
        var response = mockMvc.perform(post("/auth/authenticate").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"long-enough-password\"}")).andReturn().getResponse().getContentAsString();
        token = JsonPath.read(response, "$.token");
    }

    private ResultActions postJson(String path, String body) throws Exception
    {
        return mockMvc.perform(post(path).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void buildAndDemolish_shouldUseFlatBuildingCounts() throws Exception
    {
        postJson("/kingdom/build", "{\"house\":2}").andExpect(status().isOk()).andExpect(jsonPath("$.data.house").value(2));
        postJson("/kingdom/demolish", "{\"house\":1}").andExpect(status().isOk()).andExpect(jsonPath("$.data.house").value(1));
    }

    @Test
    void trainAndFire_shouldUseFlatUnitCounts() throws Exception
    {
        postJson("/kingdom/train", "{\"farmer\":3}").andExpect(status().isOk()).andExpect(jsonPath("$.data.farmer").value(3));
        postJson("/kingdom/fire", "{\"farmer\":2}").andExpect(status().isOk()).andExpect(jsonPath("$.data.farmer").value(2));
    }

    /**
     * the maps marked @EveryKey promise clients a value for every key, so a new kingdom must carry them all
     */
    @Test
    void aKingdom_shouldCarryEveryKeyOfItsCompleteCountMaps() throws Exception
    {
        var kingdom = mockMvc.perform(get("/kingdom").header("Authorization", "Bearer " + token)).andReturn().getResponse().getContentAsString();

        for (var resource : ResourceName.values())
        {
            assertNotNull(JsonPath.read(kingdom, "$.resources." + resource.name()), resource.name());
        }
        for (var building : BuildingName.values())
        {
            assertNotNull(JsonPath.read(kingdom, "$.buildings." + building.name()), building.name());
        }
        for (var unit : UnitName.values())
        {
            assertNotNull(JsonPath.read(kingdom, "$.units.availableUnits." + unit.name()), unit.name());
            assertNotNull(JsonPath.read(kingdom, "$.units.mobileUnits." + unit.name()), unit.name());
        }
    }

    @Test
    void sendAttack_shouldMoveTroopsAndShowThemOnTheKingdom() throws Exception
    {
        postJson("/kingdom/build", "{\"barracks\":1}").andExpect(status().isOk());
        postJson("/kingdom/train", "{\"infantry\":1}").andExpect(jsonPath("$.data.infantry").value(1));

        postJson("/kingdom/send-attack", "{\"destinationKingdomName\":\"GoldMinerBot\",\"attackType\":\"land\",\"units\":{\"infantry\":1}}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/kingdom").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.ongoingAttacks[0].to").value("GoldMinerBot"))
                .andExpect(jsonPath("$.ongoingAttacks[0].units.infantry").value(1))
                .andExpect(jsonPath("$.units.mobileUnits.infantry").value(1));
    }

    @Test
    void periodicTurn_shouldStopAtMaxTurnsAndKeepPlayerChanges() throws Exception
    {
        var maxTurns = gameConfig.common().maxTurns();
        var kingdom = kingdomRepository.getKingdomByName(kingdomName).orElseThrow();
        kingdom.getResources().setCount(ResourceName.turns, maxTurns - 1);
        kingdomRepository.update(kingdom);
        postJson("/kingdom/build", "{\"house\":1}").andExpect(jsonPath("$.data.house").value(1));
        var housesAfterBuilding = kingdomRepository.getKingdomByName(kingdomName).orElseThrow().getBuildings().getCount(BuildingName.house);

        kingdomService.addTurnForEveryone();
        kingdomService.addTurnForEveryone();

        var updated = kingdomRepository.getKingdomByName(kingdomName).orElseThrow();
        assertEquals(maxTurns, updated.getResources().getCount(ResourceName.turns));
        assertEquals(housesAfterBuilding, updated.getBuildings().getCount(BuildingName.house));
    }

    @Test
    void battleReport_shouldBeStoredAndServedAfterTheArmyArrives() throws Exception
    {
        postJson("/kingdom/build", "{\"barracks\":1}").andExpect(status().isOk());
        postJson("/kingdom/train", "{\"infantry\":1}").andExpect(jsonPath("$.data.infantry").value(1));
        postJson("/kingdom/send-attack", "{\"destinationKingdomName\":\"IronMinerBot\",\"attackType\":\"economy\",\"units\":{\"infantry\":1}}")
                .andExpect(jsonPath("$.success").value(true));

        for (int turn = 0; turn < gameConfig.attack().turnsToArrive(); turn++)
        {
            postJson("/kingdom/pass-turn", "0").andExpect(jsonPath("$.success").value(true));
        }

        mockMvc.perform(get("/war/battle-reports").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].attacker").value(kingdomName))
                .andExpect(jsonPath("$[0].defender").value("IronMinerBot"))
                .andExpect(jsonPath("$[0].unitsSent.infantry").value(1));
    }

    @Test
    void spyReport_shouldBeStoredAndServed() throws Exception
    {
        postJson("/kingdom/build", "{\"spyGuild\":1}").andExpect(status().isOk());
        postJson("/kingdom/train", "{\"spy\":1}").andExpect(jsonPath("$.data.spy").value(1));

        postJson("/war/send-spies", "{\"targetKingdomName\":\"Umbar\",\"spies\":1}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/war/spy-reports").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].targetKingdomName").value("Umbar"))
                .andExpect(jsonPath("$[0].spiesSent").value(1));
    }

    @Test
    void concurrentChanges_shouldBeRejectedInsteadOfOverwritten()
    {
        var first = kingdomRepository.getKingdomByName(kingdomName).orElseThrow();
        var second = kingdomRepository.getKingdomByName(kingdomName).orElseThrow();
        first.getResources().setCount(ResourceName.gold, 1);
        kingdomRepository.update(first);

        second.getResources().setCount(ResourceName.gold, 2);

        assertThrows(ObjectOptimisticLockingFailureException.class, () -> kingdomRepository.update(second));
        assertEquals(1, kingdomRepository.getKingdomByName(kingdomName).orElseThrow().getResources().getCount(ResourceName.gold));
    }

    @Test
    void periodicTurn_shouldCountProtectionDown()
    {
        var kingdom = kingdomRepository.getKingdomByName(kingdomName).orElseThrow();
        kingdom.setProtectionTicks(2);
        kingdomRepository.update(kingdom);

        kingdomService.addTurnForEveryone();
        assertEquals(1, kingdomRepository.getKingdomByName(kingdomName).orElseThrow().getProtectionTicks());
        kingdomService.addTurnForEveryone();
        kingdomService.addTurnForEveryone();
        assertEquals(0, kingdomRepository.getKingdomByName(kingdomName).orElseThrow().getProtectionTicks());
    }

    @Test
    void periodicTurn_shouldRejectAChangeReadBeforeIt()
    {
        var readBeforeTheTick = kingdomRepository.getKingdomByName(kingdomName).orElseThrow();
        var turnsBefore = readBeforeTheTick.getResources().getCount(ResourceName.turns);

        kingdomService.addTurnForEveryone();
        readBeforeTheTick.getResources().setCount(ResourceName.gold, 1);

        assertThrows(ObjectOptimisticLockingFailureException.class, () -> kingdomRepository.update(readBeforeTheTick));
        assertEquals(turnsBefore + 1, kingdomRepository.getKingdomByName(kingdomName).orElseThrow().getResources().getCount(ResourceName.turns));
    }

    private long version()
    {
        return jdbcTemplate.queryForObject("SELECT version FROM kingdom_entity WHERE name = ?", Long.class, kingdomName);
    }

    @Test
    void readingKingdoms_shouldNotWriteThemBack() throws Exception
    {
        postJson("/kingdom/pass-turn", "0").andExpect(jsonPath("$.success").value(true));
        var before = version();

        transactionTemplate.executeWithoutResult(status -> kingdomRepository.getAllKingdoms().forEach(kingdom -> kingdom.getName()));
        roundService.getRankings();
        mockMvc.perform(get("/kingdom/details").header("Authorization", "Bearer " + token)).andExpect(status().isOk());

        assertEquals(before, version());
    }

    private String register(String name) throws Exception
    {
        var email = "api-" + System.nanoTime() + "@example.com";
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"kingdomName\":\"" + name + "\",\"password\":\"long-enough-password\"}"))
                .andExpect(status().isOk());
        var response = mockMvc.perform(post("/auth/authenticate").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"long-enough-password\"}")).andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    @Test
    void reinforcements_shouldStandGuardWithAnAllyAndComeHomeOnRecall() throws Exception
    {
        var hostName = "Host " + (System.nanoTime() % 1000000);
        var hostToken = registerAlly(hostName);
        var kingdom = kingdomRepository.getKingdomByName(kingdomName).orElseThrow();
        kingdom.getUnits().addAvailableCount(UnitName.infantry, 20);
        kingdomRepository.update(kingdom);

        postJson("/kingdom/send-reinforcements", "{\"hostKingdomName\":\"" + hostName + "\",\"units\":{\"infantry\":15}}").andExpect(jsonPath("$.success").value(true));
        for (int turn = 0; turn < gameConfig.common().turnsToReinforce(); turn++)
        {
            postJson("/kingdom/pass-turn", "0").andExpect(jsonPath("$.success").value(true));
        }

        mockMvc.perform(get("/kingdom").header("Authorization", "Bearer " + hostToken))
                .andExpect(jsonPath("$.garrisons[0].from").value(kingdomName))
                .andExpect(jsonPath("$.garrisons[0].turnsLeft").value(0))
                .andExpect(jsonPath("$.garrisons[0].units.infantry").value(15));
        var sent = mockMvc.perform(get("/kingdom").header("Authorization", "Bearer " + token)).andReturn().getResponse().getContentAsString();
        String reinforcementId = JsonPath.read(sent, "$.reinforcements[0].id");

        postJson("/kingdom/recall-reinforcements", "\"" + reinforcementId + "\"").andExpect(status().isOk());

        mockMvc.perform(get("/kingdom").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.reinforcements.length()").value(0))
                .andExpect(jsonPath("$.units.availableUnits.infantry").value(20))
                .andExpect(jsonPath("$.units.mobileUnits.infantry").value(0));
        mockMvc.perform(get("/kingdom").header("Authorization", "Bearer " + hostToken)).andExpect(jsonPath("$.garrisons.length()").value(0));
    }

    @Test
    void anArmyOnTheMarch_shouldBeSeenComingByItsTarget() throws Exception
    {
        var targetName = "Target " + (System.nanoTime() % 1000000);
        var targetToken = register(targetName);
        var kingdom = kingdomRepository.getKingdomByName(kingdomName).orElseThrow();
        kingdom.getUnits().addAvailableCount(UnitName.cavalry, 123);
        kingdomRepository.update(kingdom);

        postJson("/kingdom/send-attack", "{\"destinationKingdomName\":\"" + targetName + "\",\"attackType\":\"land\",\"units\":{\"cavalry\":123}}")
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/kingdom").header("Authorization", "Bearer " + targetToken))
                .andExpect(jsonPath("$.incoming[0].from").value(kingdomName))
                .andExpect(jsonPath("$.incoming[0].turnsLeft").value(gameConfig.attack().turnsToArrive()))
                .andExpect(jsonPath("$.incoming[0].soldiers").value(120));
        mockMvc.perform(get("/kingdom/notifications").header("Authorization", "Bearer " + targetToken))
                .andExpect(jsonPath("$[0].kind").value("threat"))
                .andExpect(jsonPath("$[0].message").value(org.hamcrest.Matchers.containsString(kingdomName)));
    }

    /**
     * registers a kingdom and brings it into a new alliance with the logged-in one
     */
    private String registerAlly(String name) throws Exception
    {
        var allyToken = register(name);
        var allianceName = "Pact " + (System.nanoTime() % 1000000);
        postJson("/alliance/create", "{\"name\":\"" + allianceName + "\"}").andExpect(jsonPath("$.success").value(true));
        postJson("/alliance/invite", "{\"kingdomName\":\"" + name + "\",\"allianceName\":\"" + allianceName + "\"}").andExpect(jsonPath("$.success").value(true));
        var invitations = mockMvc.perform(get("/alliance/invitations").header("Authorization", "Bearer " + allyToken)).andReturn().getResponse().getContentAsString();
        String invitationId = JsonPath.read(invitations, "$[0].id");
        mockMvc.perform(post("/alliance/accept").header("Authorization", "Bearer " + allyToken).contentType(MediaType.APPLICATION_JSON)
                .content("{\"invitationId\":\"" + invitationId + "\"}")).andExpect(jsonPath("$.success").value(true));
        return allyToken;
    }

    private void arm(String name, int cavalry)
    {
        var kingdom = kingdomRepository.getKingdomByName(name).orElseThrow();
        kingdom.getUnits().addAvailableCount(UnitName.cavalry, cavalry);
        kingdomRepository.update(kingdom);
    }

    private ResultActions postJsonAs(String bearer, String path, String body) throws Exception
    {
        return mockMvc.perform(post(path).header("Authorization", "Bearer " + bearer).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void anArmyJoiningAnAllysStrike_shouldWaitAtTheBorderAndLandRightAfterIt() throws Exception
    {
        var leaderName = "Leader " + (System.nanoTime() % 1000000);
        var leaderToken = registerAlly(leaderName);
        var targetName = "Target " + (System.nanoTime() % 1000000);
        register(targetName);
        arm(leaderName, 50);
        arm(kingdomName, 30);
        postJsonAs(leaderToken, "/kingdom/send-attack", "{\"destinationKingdomName\":\"" + targetName + "\",\"attackType\":\"land\",\"units\":{\"cavalry\":50}}")
                .andExpect(jsonPath("$.success").value(true));
        var leader = mockMvc.perform(get("/kingdom").header("Authorization", "Bearer " + leaderToken)).andReturn().getResponse().getContentAsString();
        String leadId = JsonPath.read(leader, "$.ongoingAttacks[0].id");

        postJson("/kingdom/send-attack", "{\"destinationKingdomName\":\"" + targetName + "\",\"attackType\":\"land\",\"units\":{\"cavalry\":30},\"joins\":\"" + leadId + "\"}")
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/kingdom/notifications").header("Authorization", "Bearer " + leaderToken))
                .andExpect(jsonPath("$[0].message").value(org.hamcrest.Matchers.containsString(kingdomName + " joins our strike on " + targetName)));

        // ours reaches the border first and waits there for the leader's army
        for (int turn = 0; turn < gameConfig.attack().turnsToArrive() + 2; turn++)
        {
            postJson("/kingdom/pass-turn", "0").andExpect(jsonPath("$.success").value(true));
        }
        mockMvc.perform(get("/kingdom").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.ongoingAttacks[0].turnsLeft").value(0))
                .andExpect(jsonPath("$.ongoingAttacks[0].joining").value(leaderName));
        mockMvc.perform(get("/war/battle-reports").header("Authorization", "Bearer " + token)).andExpect(jsonPath("$.length()").value(0));

        for (int turn = 0; turn < gameConfig.attack().turnsToArrive(); turn++)
        {
            postJsonAs(leaderToken, "/kingdom/pass-turn", "0").andExpect(jsonPath("$.success").value(true));
        }

        mockMvc.perform(get("/kingdom").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.ongoingAttacks.length()").value(0))
                .andExpect(jsonPath("$.units.mobileUnits.cavalry").value(0));
        mockMvc.perform(get("/war/battle-reports").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[0].attacker").value(kingdomName))
                .andExpect(jsonPath("$[0].defender").value(targetName))
                .andExpect(jsonPath("$[0].unitsSent.cavalry").value(30));
        mockMvc.perform(get("/war/battle-reports").header("Authorization", "Bearer " + leaderToken)).andExpect(jsonPath("$[0].attacker").value(leaderName));
    }

    @Test
    void joiningAnArmyThatItselfJoined_shouldJoinTheStrikesLead() throws Exception
    {
        var leaderName = "Leader " + (System.nanoTime() % 1000000);
        var leaderToken = registerAlly(leaderName);
        var allianceName = kingdomRepository.getKingdomByName(kingdomName).orElseThrow().getAlliance().orElseThrow().getName();
        var thirdName = "Third " + (System.nanoTime() % 1000000);
        var thirdToken = register(thirdName);
        postJson("/alliance/invite", "{\"kingdomName\":\"" + thirdName + "\",\"allianceName\":\"" + allianceName + "\"}").andExpect(jsonPath("$.success").value(true));
        var invitations = mockMvc.perform(get("/alliance/invitations").header("Authorization", "Bearer " + thirdToken)).andReturn().getResponse().getContentAsString();
        String invitationId = JsonPath.read(invitations, "$[0].id");
        postJsonAs(thirdToken, "/alliance/accept", "{\"invitationId\":\"" + invitationId + "\"}").andExpect(jsonPath("$.success").value(true));
        var targetName = "Target " + (System.nanoTime() % 1000000);
        register(targetName);
        arm(leaderName, 10);
        arm(kingdomName, 10);
        arm(thirdName, 10);

        postJsonAs(leaderToken, "/kingdom/send-attack", "{\"destinationKingdomName\":\"" + targetName + "\",\"attackType\":\"land\",\"units\":{\"cavalry\":10}}");
        String leadId = JsonPath.read(mockMvc.perform(get("/kingdom").header("Authorization", "Bearer " + leaderToken)).andReturn().getResponse().getContentAsString(),
                "$.ongoingAttacks[0].id");
        postJson("/kingdom/send-attack", "{\"destinationKingdomName\":\"" + targetName + "\",\"attackType\":\"land\",\"units\":{\"cavalry\":10},\"joins\":\"" + leadId + "\"}");
        String ourId = JsonPath.read(mockMvc.perform(get("/kingdom").header("Authorization", "Bearer " + token)).andReturn().getResponse().getContentAsString(),
                "$.ongoingAttacks[0].id");

        postJsonAs(thirdToken, "/kingdom/send-attack", "{\"destinationKingdomName\":\"" + targetName + "\",\"attackType\":\"land\",\"units\":{\"cavalry\":10},\"joins\":\"" + ourId + "\"}")
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/kingdom").header("Authorization", "Bearer " + thirdToken)).andExpect(jsonPath("$.ongoingAttacks[0].joining").value(leaderName));
    }

    @Test
    void anAllysDearOffer_shouldBeListedForAlliesAndPassThemGold() throws Exception
    {
        var allyName = "Warlord " + (System.nanoTime() % 1000000);
        var allyToken = registerAlly(allyName);
        var kingdom = kingdomRepository.getKingdomByName(kingdomName).orElseThrow();
        kingdom.getResources().setCount(ResourceName.gold, 60000);
        kingdomRepository.update(kingdom);
        var allyGold = kingdomRepository.getKingdomByName(allyName).orElseThrow().getResources().getCount(ResourceName.gold);
        mockMvc.perform(post("/market/create").header("Authorization", "Bearer " + allyToken).contentType(MediaType.APPLICATION_JSON)
                .content("{\"resource\":\"food\",\"count\":1,\"price\":50000}")).andExpect(jsonPath("$.success").value(true));

        var listed = mockMvc.perform(get("/market/allies").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].sellerName").value(allyName))
                .andExpect(jsonPath("$[0].price").value(50000))
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(get("/market/allies").header("Authorization", "Bearer " + allyToken)).andExpect(jsonPath("$.length()").value(0));
        String offerId = JsonPath.read(listed, "$[0].id");

        postJson("/market/" + offerId + "/buy", "{\"count\":1}").andExpect(jsonPath("$.data.totalCost").value(50000));

        assertEquals(10000, kingdomRepository.getKingdomByName(kingdomName).orElseThrow().getResources().getCount(ResourceName.gold));
        assertEquals(allyGold + 50000, kingdomRepository.getKingdomByName(allyName).orElseThrow().getResources().getCount(ResourceName.gold));
        mockMvc.perform(get("/market/allies").header("Authorization", "Bearer " + token)).andExpect(jsonPath("$.length()").value(0));
    }
}
