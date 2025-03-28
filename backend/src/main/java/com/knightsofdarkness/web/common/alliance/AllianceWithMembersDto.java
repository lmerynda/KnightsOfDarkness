package com.knightsofdarkness.web.common.alliance;

import java.util.List;

public record AllianceWithMembersDto(String name, String emperor, List<String> members) {

}
