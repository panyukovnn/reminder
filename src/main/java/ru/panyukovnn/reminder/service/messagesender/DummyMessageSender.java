package ru.panyukovnn.reminder.service.messagesender;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Пустая реализация сервиса отправки сообщений.
 */
@Service
@Primary
@ConditionalOnProperty(value = "reminder.send-messages", havingValue = "false")
public class DummyMessageSender implements MessageSender {
    // Реализация не требуется
}
