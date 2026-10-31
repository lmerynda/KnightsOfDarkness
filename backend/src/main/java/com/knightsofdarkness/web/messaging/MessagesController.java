package com.knightsofdarkness.web.messaging;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.knightsofdarkness.web.common.messaging.MessageDto;
import com.knightsofdarkness.web.common.messaging.SendMessageDto;
import com.knightsofdarkness.web.common.messaging.SendMessageResult;
import com.knightsofdarkness.web.user.UserData;

@RestController
@RequestMapping("/messages")
public class MessagesController {
    private static final Logger log = LoggerFactory.getLogger(MessagesController.class);
    private final MessageService messageService;

    public MessagesController(MessageService messageService)
    {
        this.messageService = messageService;
    }

    @GetMapping("/inbox")
    ResponseEntity<List<MessageDto>> getInbox(@AuthenticationPrincipal UserData currentUser)
    {
        if (currentUser == null)
        {
            return unauthorized();
        }

        return ResponseEntity.ok(messageService.getInbox(currentUser.getKingdomName()));
    }

    @GetMapping("/sent")
    ResponseEntity<List<MessageDto>> getSent(@AuthenticationPrincipal UserData currentUser)
    {
        if (currentUser == null)
        {
            return unauthorized();
        }

        return ResponseEntity.ok(messageService.getSent(currentUser.getKingdomName()));
    }

    @GetMapping("/unread-count")
    ResponseEntity<Long> countUnread(@AuthenticationPrincipal UserData currentUser)
    {
        if (currentUser == null)
        {
            return unauthorized();
        }

        return ResponseEntity.ok(messageService.countUnread(currentUser.getKingdomName()));
    }

    @PostMapping("/send")
    ResponseEntity<SendMessageResult> send(@AuthenticationPrincipal UserData currentUser, @RequestBody SendMessageDto sendMessageDto)
    {
        if (currentUser == null)
        {
            return unauthorized();
        }

        return ResponseEntity.ok(messageService.send(currentUser.getKingdomName(), sendMessageDto.recipient(), sendMessageDto.body()));
    }

    @PostMapping("/send-alliance")
    ResponseEntity<SendMessageResult> sendToAlliance(@AuthenticationPrincipal UserData currentUser, @RequestBody SendMessageDto sendMessageDto)
    {
        if (currentUser == null)
        {
            return unauthorized();
        }

        return ResponseEntity.ok(messageService.sendToAlliance(currentUser.getKingdomName(), sendMessageDto.body()));
    }

    @PostMapping("/{id}/mark-as-read")
    ResponseEntity<Boolean> markAsRead(@AuthenticationPrincipal UserData currentUser, @PathVariable UUID id)
    {
        if (currentUser == null)
        {
            return unauthorized();
        }

        return messageService.markAsRead(currentUser.getKingdomName(), id) ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    private static <T> ResponseEntity<T> unauthorized()
    {
        log.error("User not read from authentication context");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
