package ru.panyukovnn.reminder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.panyukovnn.reminder.exception.TimeCheckerException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Сервис проверки времени запуска.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionTimeChecker {

    private static final List<DayOfWeek> WEEK_DAY_BLACK_LIST = List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    private final Set<LocalDate> holidays;

    public void checkHoliday() {
        // Если не удалось распознать производственный календарь из ресурсов, то проверяем является ли сегодняшний день будним
        if (CollectionUtils.isEmpty(holidays) && WEEK_DAY_BLACK_LIST.contains(LocalDate.now().getDayOfWeek())) {
            String errMsg = String.format("Нерабочий день, не допускается запуск по следующим дням: %s", WEEK_DAY_BLACK_LIST);

            throw new TimeCheckerException(errMsg);
        }

        if (holidays.contains(LocalDate.now())) {
            throw new TimeCheckerException("Выходной день по производственному календарю, запуск отменен");
        }
    }

}
