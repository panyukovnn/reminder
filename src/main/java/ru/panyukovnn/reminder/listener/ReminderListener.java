package ru.panyukovnn.reminder.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.panyukovnn.reminder.config.BotApi;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderListener {

    private final BotApi botApi;

    @EventListener(Update.class)
    public void onStartup(Update update) {
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

        processStickerIfAny(message);

        User user = message.getFrom();

        String username = Optional.ofNullable(user).map(User::getUserName).orElse("undefined");
        String firstname = Optional.ofNullable(user).map(User::getFirstName).orElse("undefined");
        String lastname = Optional.ofNullable(user).map(User::getLastName).orElse("undefined");

        String messageText = Optional.ofNullable(message.getText())
            .orElse("undefined");

        Long chatId = Optional.ofNullable(message.getChatId())
            .orElse(0L);

        log.info("Входящее update сообщение в чате: {}. Текст: {}. От: {}", chatId, messageText, username + " " + firstname + " " + lastname + " ");
    }

    private void processStickerIfAny(Message message) {
        if (message.getSticker() != null && message.getFrom() != null) {
            Sticker sticker = message.getSticker();
            User user = message.getFrom();

            if (!message.getChatId().equals(user.getId())) {
                // Если это не персональная переписка, то не присылаем стикер

                return;
            }

            SendMessage sendMessage = new SendMessage();

            sendMessage.setChatId(message.getChatId());
            sendMessage.setText("Идентификатор стикера: " + sticker.getFileId() + "\nНаименование стикер сета: " + sticker.getSetName());

            try {
                botApi.execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке сообдщения о стикере клиенту {}: {}", message.getChatId(), e.getMessage(), e);
            }
        }
    }
}
