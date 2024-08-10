package ru.panyukovnn.reminder.dto;

import lombok.Data;

import java.util.List;

@Data
public class YearHoliday {

    private int year;
    private List<MonthHoliday> months;
}
