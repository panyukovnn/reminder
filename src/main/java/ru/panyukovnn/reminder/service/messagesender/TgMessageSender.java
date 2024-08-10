package ru.panyukovnn.reminder.service.messagesender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.panyukovnn.reminder.exception.ReminderException;
import ru.panyukovnn.reminder.property.ReminderProperty;

import java.time.Duration;

/**
 * Сервис отправки сообщений с помощью Телеграм.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TgMessageSender implements MessageSender {

    private static final int TIMEOUT_S = 30;

    private final WebClient webClient;
    private final ReminderProperty reminderProperty;

    @Override
    public Mono<String> sendMessage(String chatId, String message) {
        UriComponents uri = UriComponentsBuilder
                .fromHttpUrl("https://api.telegram.org")
                .path("/bot" + reminderProperty.getBotToken() + "/sendMessage")
                .queryParam("chat_id", chatId)
                .queryParam("text", message)
                .queryParam("parse_mode", "html")
                .queryParam("disable_web_page_preview", true)
                .build();

        return webClient.get()
                .uri(uri.toUri())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(TIMEOUT_S))
                .doOnNext(item -> log.info("Сообщение отправлено в группу {}", chatId))
                .onErrorMap(WebClientResponseException.class, error -> new ReminderException(String.format("Ошибка при отправке сообщения в группу рассылки. Статус: %s. Тело ответа: %s",
                        error.getStatusCode(),
                        error.getResponseBodyAsString())));
    }

    @Override
    public Mono<String> sendSticker(String chatId, String stickerFileId) {
        UriComponents uri = UriComponentsBuilder
                .fromHttpUrl("https://api.telegram.org")
                .path("/bot" + reminderProperty.getBotToken() + "/sendSticker")
                .queryParam("chat_id", chatId)
                .queryParam("sticker", stickerFileId)
                .queryParam("disable_notification", true)
                .build();

        return webClient.get()
                .uri(uri.toUri())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(TIMEOUT_S))
                .doOnNext(item -> log.info("Стикер отправлен в группу {}", chatId))
                .onErrorMap(WebClientResponseException.class, error -> new ReminderException(String.format("Ошибка при отправке стикера в группу рассылки. Статус: %s. Тело ответа: %s",
                        error.getStatusCode(),
                        error.getResponseBodyAsString())));
    }
}
