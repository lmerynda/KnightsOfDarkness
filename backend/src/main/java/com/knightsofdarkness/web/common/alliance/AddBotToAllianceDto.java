package com.knightsofdarkness.web.common.alliance;

import java.util.Objects;

public record AddBotToAllianceDto(String botName) {
    /** a request without these is refused as malformed, before it reaches the game */
    public AddBotToAllianceDto
    {
        Objects.requireNonNull(botName, "botName");
    }
}
