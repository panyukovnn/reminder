package ru.panyukovnn.reminder.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class NotificationInfo {

    @NotEmpty(message = "Не задан name")
    private String name;
    private boolean active;
    private boolean checkHolidays;

    @NotEmpty(message = "Не задан chatId")
    private String chatId;

    @NotEmpty(message = "Не задан cron")
    private String cron;

    private List<String> stickers;

    @Valid
    private TextMessageConfig textMessage;

}
