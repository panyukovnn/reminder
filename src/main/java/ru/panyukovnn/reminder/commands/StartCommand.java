package ru.panyukovnn.reminder.commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
public class StartCommand extends BotCommand {

    private static final String GREETING_MESSAGE = "Работаю";

    public StartCommand() {
        super("start", "Start command");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(chat.getId());
        sendMessage.setText(GREETING_MESSAGE);

        try {
            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке приветственного сообщения клиенту {}: {}", chat.getId(), e.getMessage(), e);
        }
    }
}