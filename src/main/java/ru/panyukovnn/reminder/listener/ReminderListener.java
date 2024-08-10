package ru.panyukovnn.reminder.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

import static ru.panyukovnn.reminder.config.ReminderConfig.TG_UPDATE_SINK;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderListener {

    @EventListener(ApplicationStartedEvent.class)
    public void onStartup() {
        TG_UPDATE_SINK.asFlux()
                .doOnNext(update -> {
                    if (update == null) {
                        return;
                    }

                    Message message = update.getMessage() != null
                            ? update.getMessage()
                            : update.getChannelPost();

                    if (message == null) {
                        log.warn("Получено пустое сообщение: {}", update);

                        return;
                    }

                    User user = message.getFrom();

                    String username = Optional.ofNullable(user).map(User::getUserName).orElse("undefined");
                    String firstname = Optional.ofNullable(user).map(User::getFirstName).orElse("undefined");
                    String lastname = Optional.ofNullable(user).map(User::getLastName).orElse("undefined");

                    String messageText = Optional.ofNullable(message.getText())
                            .orElse("undefined");

                    Long chatId = Optional.ofNullable(message.getChatId())
                            .orElse(0L);

                    log.info("Входящее update сообщение в чате: {}. Текст: {}. От: {}", chatId, messageText, username + " " + firstname + " " + lastname + " ");
                })
                .subscribe();
    }
}
