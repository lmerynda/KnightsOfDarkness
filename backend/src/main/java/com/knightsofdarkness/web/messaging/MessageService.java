package com.knightsofdarkness.web.messaging;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.web.common.messaging.MessageDto;
import com.knightsofdarkness.web.common.messaging.SendMessageResult;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomRepository;
import com.knightsofdarkness.web.messaging.model.MessageEntity;
import com.knightsofdarkness.web.messaging.model.MessageJpaRepository;
import com.knightsofdarkness.web.utils.Id;
import com.knightsofdarkness.web.utils.Utils;

@Service
public class MessageService {
    public static final int maxBodyLength = 2000;
    private static final int pageSize = 50;
    private final MessageJpaRepository messageRepository;
    private final KingdomRepository kingdomRepository;

    public MessageService(MessageJpaRepository messageRepository, KingdomRepository kingdomRepository)
    {
        this.messageRepository = messageRepository;
        this.kingdomRepository = kingdomRepository;
    }

    @Transactional
    public SendMessageResult send(String sender, String recipient, String body)
    {
        var bodyError = validateBody(body);
        if (bodyError != null)
        {
            return SendMessageResult.failure(bodyError);
        }
        if (recipient == null || recipient.equals(sender))
        {
            return SendMessageResult.failure("Choose another kingdom to write to");
        }
        if (kingdomRepository.getKingdomByName(recipient).isEmpty())
        {
            return SendMessageResult.failure(Utils.format("Kingdom {} does not exist", recipient));
        }

        messageRepository.save(new MessageEntity(Id.generate(), sender, recipient, body.strip(), Instant.now(), false));
        return SendMessageResult.success(Utils.format("Message sent to {}", recipient), 1);
    }

    @Transactional
    public SendMessageResult sendToAlliance(String sender, String body)
    {
        var bodyError = validateBody(body);
        if (bodyError != null)
        {
            return SendMessageResult.failure(bodyError);
        }

        var maybeAlliance = kingdomRepository.getKingdomByName(sender).flatMap(KingdomEntity::getAlliance);
        if (maybeAlliance.isEmpty())
        {
            return SendMessageResult.failure("You are not in an alliance");
        }

        var now = Instant.now();
        var recipients = maybeAlliance.get().getKingdoms().stream().map(KingdomEntity::getName).filter(name -> !name.equals(sender)).toList();
        if (recipients.isEmpty())
        {
            return SendMessageResult.failure("Your alliance has no other members yet");
        }

        recipients.forEach(recipient -> messageRepository.save(new MessageEntity(Id.generate(), sender, recipient, body.strip(), now, true)));
        return SendMessageResult.success(Utils.format("Message sent to {} allies", recipients.size()), recipients.size());
    }

    private static String validateBody(String body)
    {
        if (body == null || body.isBlank())
        {
            return "Message cannot be empty";
        }
        if (body.strip().length() > maxBodyLength)
        {
            return Utils.format("Message cannot be longer than {} characters", maxBodyLength);
        }
        return null;
    }

    public List<MessageDto> getInbox(String kingdomName)
    {
        return messageRepository.findByRecipientOrderByDateDesc(kingdomName, PageRequest.of(0, pageSize)).stream().map(MessageEntity::toDto).toList();
    }

    public List<MessageDto> getSent(String kingdomName)
    {
        return messageRepository.findBySenderOrderByDateDesc(kingdomName, PageRequest.of(0, pageSize)).stream().map(MessageEntity::toDto).toList();
    }

    public long countUnread(String kingdomName)
    {
        return messageRepository.countByRecipientAndIsReadFalse(kingdomName);
    }

    @Transactional
    public boolean markAsRead(String kingdomName, UUID messageId)
    {
        var maybeMessage = messageRepository.findById(messageId).filter(message -> message.getRecipient().equals(kingdomName));
        maybeMessage.ifPresent(message ->
        {
            message.markAsRead();
            messageRepository.save(message);
        });
        return maybeMessage.isPresent();
    }
}
