package com.merynda;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.merynda.kod.kingdom.Kingdom;

@RestController
public class KingdomController {
    // TODO a rest controller that returns a kingdom by id
    @RequestMapping("/kingdom")
    Kingdom getKingdom()
    {
        Kingdom kingdom = new Kingdom(null, null, null, null, null);

        return kingdom;
    }
}
