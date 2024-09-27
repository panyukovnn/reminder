package ru.panyukovnn.reminder.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.stickers.GetStickerSet;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import ru.panyukovnn.reminder.config.BotApi;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class MessagePicker {

    private BotApi botApi;

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

    public List<String> extractStickerSetStickers(String stickerSetName) {
        if (!StringUtils.hasText(stickerSetName)) {
            return List.of();
        }

        try {
            return botApi.execute(GetStickerSet.builder()
                .name(stickerSetName)
                .build())
                .getStickers().stream()
                .map(Sticker::getFileId)
                .toList();
        } catch (Exception e) {
            log.warn("Ошибка при запросе стикерсета по наименованию: {}", stickerSetName, e);

            return List.of();
        }
    }
}
