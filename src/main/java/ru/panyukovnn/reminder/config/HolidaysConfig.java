package ru.panyukovnn.reminder.config;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.CollectionUtils;
import ru.panyukovnn.reminder.dto.YearHoliday;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class HolidaysConfig {

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    @Bean
    public Set<LocalDate> holidays() {
        Year year = Year.now();

        String holidaysFileName = "holidays-" + year.getValue() + ".json";
        try {
            InputStream developersFileInputStream = resourceLoader.getResource("classpath:" + holidaysFileName).getInputStream();
            YearHoliday yearHolidaysFromFile = objectMapper.readValue(developersFileInputStream, new TypeReference<>() {
            });

            if (yearHolidaysFromFile == null
                    || year.getValue() != yearHolidaysFromFile.getYear()
                    || CollectionUtils.isEmpty(yearHolidaysFromFile.getMonths())) {
                log.error("Ошибка валидации файла выходных дней: \"{}\". В файле либо отсутствуют данные, либо не совпал год", holidaysFileName);

                return new HashSet<>();
            }

            return yearHolidaysFromFile.getMonths().stream()
                .flatMap(it -> {
                    Month month = Month.of(it.getMonth());

                    return Arrays.stream(it.getDays().split(","))
                        .map(dayNum -> LocalDate.of(year.getValue(), month, Integer.parseInt(dayNum)));
                })
                .collect(Collectors.toUnmodifiableSet());
        } catch (Exception e) {
            log.error("Ошибка чтения списка выходных дней из файла \"{}\": {}", holidaysFileName, e.getMessage(), e);

            return new HashSet<>();
        }
    }
}
