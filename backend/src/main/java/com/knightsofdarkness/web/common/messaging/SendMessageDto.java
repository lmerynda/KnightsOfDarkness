package com.knightsofdarkness.web.common.messaging;

/**
 * recipient is ignored when sending to the whole alliance
 */
public record SendMessageDto(String recipient, String body) {
}
