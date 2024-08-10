package ru.panyukovnn.reminder.property;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Getter
@Setter
@ToString
@Component
@Validated
@ConfigurationProperties("reminder")
public class ReminderProperty {

    /**
     * Токен телеграм бота.
     */
    @NotEmpty(message = "Не задан токен бота")
    private String botToken;
    /**
     * Имя бота.
     */
    @NotEmpty(message = "Не задано имя бота")
    private String botName;
    /**
     * Идентификатор чата для информирования об исключительных ситуациях.
     */
    @NotEmpty(message = "Не задан идентификатор чата для информирования об исключительных ситуациях")
    private String debugChatId;
    /**
     * Признак отправки сообщения.
     */
    private boolean sendMessages;
    /**
     * Путь до файлов с информацией об уведомлениях
     */
    private String notificationConfigPath;
    /**
     * Токен github для доступа к репозиторию с конфигурацией.
     */
    private String githubProjectConfigsToken;

    public void pc() {
        log.info(this.toString());
    }
}
