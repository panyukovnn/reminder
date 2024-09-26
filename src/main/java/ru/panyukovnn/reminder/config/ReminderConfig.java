package ru.panyukovnn.reminder.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.panyukovnn.reminder.property.ReminderProperty;

import java.util.List;

/**
 * Общая конфигурация приложения.
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ReminderConfig {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean
    public BotApi botApi(ReminderProperty reminderProperty,
                         TelegramBotsApi telegramBotsApi,
                         List<BotCommand> commands) throws TelegramApiException {
        BotApi botApi = new BotApi(reminderProperty.getBotName(), reminderProperty.getBotToken(), applicationEventPublisher);

        telegramBotsApi.registerBot(botApi);
        commands.forEach(botApi::register);

        return botApi;
    }
}
