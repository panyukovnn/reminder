package ru.panyukovnn.reminder.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MessagePicker {

    public String pickRandomSticker(List<String> stickerIds) {
        int todayMessageIndex = randomIndexByDayOfMonth(stickerIds.size());

        return stickerIds.get(todayMessageIndex);
    }

    public String pickRandomBodyMessage(List<String> bodies) {
        int todayMessageIndex = randomIndexByDayOfMonth(bodies.size());

        return bodies.get(todayMessageIndex);
    }

    private int randomIndexByDayOfMonth(int listSize) {
        return LocalDate.now().getDayOfMonth() % listSize;
    }
}
