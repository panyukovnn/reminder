package ru.panyukovnn.reminder.service.messagesender;

import reactor.core.publisher.Mono;

/**
 * Интерфейс отправки сообщений.
 */
public interface MessageSender {

    /**
     * Выполняет отправку сообщения.
     *
     * @param chatId  идентификатор чата
     * @param message текст сообщения
     * @return поток выполнения запроса
     */
    default Mono<String> sendMessage(String chatId, String message) {
        return Mono.empty();
    }

    /**
     * Выполняет отправку стикера телеграм.
     *
     * @param chatId        идентификатор чата
     * @param stickerFileId идентификатор файла стикера
     * @return поток выполнения запроса
     */
    default Mono<String> sendSticker(String chatId, String stickerFileId) {
        return Mono.empty();
    }
}
