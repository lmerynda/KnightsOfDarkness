package com.merynda.kod;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.merynda.kod.gameconfig.SpecialBuildingCosts;

public class Main {
    public static void main(String[] args) throws JsonProcessingException
    {
        System.out.println("Hello world!");

        var specialBuildingCosts = new SpecialBuildingCosts(10000, 10000, 5000, 5000, 3000);

        var mapper = new ObjectMapper();
        var json = mapper.writeValueAsString(specialBuildingCosts);

        System.out.println(json);
    }
}