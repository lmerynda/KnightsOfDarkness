package com.knightsofdarkness.web.Kingdom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KingdomController {
    @Autowired
    private KingdomService kingdomService;

    @PostMapping("/kingdom/add")
    void createKingdom(@RequestBody KingdomDto kingdom)
    {
        kingdomService.createKingdom(kingdom);
    }

    @GetMapping("/kingdom/{name}")
    KingdomDto getKingdomByName(@PathVariable String name)
    {
        return kingdomService.getKingdomByName(name);
    }
}
