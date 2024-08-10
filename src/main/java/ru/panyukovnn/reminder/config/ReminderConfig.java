package ru.panyukovnn.reminder.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import reactor.core.publisher.Sinks;
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

    public static final Sinks.Many<Update> TG_UPDATE_SINK = Sinks.many().unicast().onBackpressureBuffer();

    private static final Sinks.EmitFailureHandler emitFailureHandler = (signalType, emitResult) -> emitResult
            .equals(Sinks.EmitResult.FAIL_NON_SERIALIZED);

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean
    public BotApi botApi(ReminderProperty reminderProperty,
                         TelegramBotsApi telegramBotsApi,
                         List<BotCommand> commands) throws TelegramApiException {
        BotApi botApi = new BotApi(reminderProperty.getBotName(), reminderProperty.getBotToken());

        telegramBotsApi.registerBot(botApi);
        commands.forEach(botApi::register);

        return botApi;
    }

    @RequiredArgsConstructor
    public static class BotApi extends TelegramLongPollingCommandBot {

        private final String username;
        private final String token;

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
            TG_UPDATE_SINK.emitNext(update, emitFailureHandler);
        }
    }
}
