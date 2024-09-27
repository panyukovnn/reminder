package ru.panyukovnn.reminder.commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.panyukovnn.reminder.service.NotificationInfoLoader;

@Slf4j
@Service
public class ReloadNotificationsCommand extends BotCommand {

    @Lazy
    @Autowired
    private NotificationInfoLoader notificationInfoLoader;

    public ReloadNotificationsCommand() {
        super("reload", "Reload notifications");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.info("Получена команда reload от пользователя: {}", user);

        notificationInfoLoader.loadNotifications()
                .doOnSuccess(it -> extracted(absSender, chat, "Уведомления успешно обновлены"))
                .doOnError(ex -> {
                    extracted(absSender, chat, "Ошибка при обновлении уведомлений из репозитория");

                    log.error("Ошибка при обновлении уведомлений из репозитория: {}", ex.getMessage(), ex);
                })
                .subscribe();
    }

    private static void extracted(AbsSender absSender, Chat chat, String message) {
        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(chat.getId());
        sendMessage.setText(message);

        try {
            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке приветственного сообщения клиенту {}: {}", chat.getId(), e.getMessage(), e);
        }
    }
}