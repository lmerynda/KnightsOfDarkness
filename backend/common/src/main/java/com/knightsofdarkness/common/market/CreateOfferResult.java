package com.knightsofdarkness.common.market;

import java.util.Optional;

public record CreateOfferResult(String message, boolean success, Optional<OfferDto> data) {

}
