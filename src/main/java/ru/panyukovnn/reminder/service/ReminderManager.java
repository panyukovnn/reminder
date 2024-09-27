package ru.panyukovnn.reminder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import reactor.core.publisher.Mono;
import ru.panyukovnn.reminder.dto.NotificationInfo;
import ru.panyukovnn.reminder.dto.TextMessageConfig;
import ru.panyukovnn.reminder.property.ReminderProperty;
import ru.panyukovnn.reminder.service.messagesender.MessageSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderManager {

    private final MessageSender messageSender;
    private final MessagePicker messagePicker;
    private final ReminderProperty reminderProperty;
    private final ExecutionTimeChecker timeChecker;

    public void processNotificationInfo(boolean isForce, boolean isDebug, NotificationInfo notificationInfo) {
        if (!isForce && notificationInfo.isCheckHolidays()) {
            timeChecker.checkHoliday();
        }

        String chatId = isDebug
            ? reminderProperty.getDebugChatId()
            : notificationInfo.getChatId();

        Mono<?> stickerSendMono = getStickerSendMono(notificationInfo, chatId);

        stickerSendMono
            .then(Mono.defer(() -> {
                if (StringUtils.hasText(notificationInfo.getVanguardMessage())) {
                    return messageSender.sendMessage(chatId, notificationInfo.getVanguardMessage());
                }

                return Mono.empty().then();
            }))
            .then(messageSender.sendMessage(chatId, formatMessage(notificationInfo.getTextMessage()))
                .doOnSuccess(item -> log.info("Отправка напоминания '{}' выполнена успешно.", notificationInfo.getName())))
            .onErrorResume(e -> {
                String errMsg = "Возникла ошибка при отправке сообщения: " + e.getMessage() + ". Во время обработки напоминания: " + notificationInfo.getName();

                log.error(errMsg, e);

                return messageSender.sendMessage(reminderProperty.getDebugChatId(), errMsg)
                    .doOnError(ex -> log.info("Произошла ошибка при отрпавке debug сообщения: {}. Во время обработки напоминания: {}", ex.getMessage(), notificationInfo.getName(), ex))
                    .doOnSuccess(item -> log.info("Отправка информации об исключительной ситуации выполнена успешно. Во время обработки напоминания: {}", notificationInfo.getName()));
            })
            .subscribe();
    }

    private Mono<?> getStickerSendMono(NotificationInfo notificationInfo, String chatId) {
        return Mono.defer(() -> {
            List<String> stickerSetIds = messagePicker.extractStickerSetStickers(notificationInfo.getStickerSetName());

            if (!CollectionUtils.isEmpty(notificationInfo.getStickers())) {
                stickerSetIds = Stream.concat(stickerSetIds.stream(), notificationInfo.getStickers().stream())
                    .toList();

            }

            if (stickerSetIds.isEmpty()) {
                return Mono.empty().then();
            }

            return sendStickerMono(notificationInfo.getName(), stickerSetIds, chatId);
        });
    }

    private String formatMessage(TextMessageConfig textMessage) {
        List<String> messageLines = new ArrayList<>();

        if (StringUtils.hasText(textMessage.getPrefix())) {
            messageLines.add(textMessage.getPrefix());
            messageLines.add(Strings.EMPTY);
        }

        if (!CollectionUtils.isEmpty(textMessage.getBodies())) {
            messageLines.add(messagePicker.pickRandomBodyMessage(textMessage.getBodies()));
        }

        if (StringUtils.hasText(textMessage.getSuffix())) {
            messageLines.add(Strings.EMPTY);
            messageLines.add(textMessage.getSuffix());
        }

        return String.join("\n", messageLines);
    }

    private Mono<Void> sendStickerMono(String notificationInfoName, List<String> stickerFileIds, String chatId) {
        return messageSender.sendSticker(chatId, messagePicker.pickRandomSticker(stickerFileIds))
            .onErrorResume(e -> {
                log.error("Непредвиденная ошибка при отправке стикера: {}. Во время обработки напоминания: {}", e.getMessage(), notificationInfoName, e);

                return Mono.empty();
            })
            .then();
    }
}
