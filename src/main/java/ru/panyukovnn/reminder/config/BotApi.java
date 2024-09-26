package ru.panyukovnn.reminder.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
public class BotApi extends TelegramLongPollingCommandBot {

    private final String username;
    private final String token;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        applicationEventPublisher.publishEvent(update);
    }
}