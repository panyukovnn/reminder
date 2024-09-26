package ru.panyukovnn.reminder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.stickers.GetStickerSet;
import org.telegram.telegrambots.meta.api.objects.stickers.StickerSet;
import ru.panyukovnn.reminder.config.BotApi;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MessagePicker {

    @Lazy // TODO
    @Autowired
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

    public Optional<StickerSet> extractStickerSet(String stickerSetName) {
        if (!StringUtils.hasText(stickerSetName)) {
            return Optional.empty();
        }

        try {
            return Optional.of(botApi.execute(GetStickerSet.builder()
                .name(stickerSetName)
                .build()));
        } catch (Exception e) {
            log.warn("Ошибка при запросе стикерсета по наименованию: {}", stickerSetName, e);

            return Optional.empty();
        }
    }
}
