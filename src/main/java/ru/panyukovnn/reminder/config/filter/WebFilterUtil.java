package ru.panyukovnn.reminder.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.ContextView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebFilterUtil {

    private final ObjectMapper objectMapper;

    public <T, V> Optional<V> readResponseBody(T body, Class<V> clazz) {
        try {
            byte[] bytes = objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);

            return Optional.of(objectMapper.readValue(bytes, clazz));
        } catch (IOException e) {
            log.warn("Ошибка при вычитывании ответа", e);
        }

        return Optional.empty();
    }

    public String readResponseBodyString(Object body) {
        try {
            byte[] bytes = objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);

            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Ошибка при вычитывании ответа", e);
        }

        return Strings.EMPTY;
    }

    public void runAsync(Runnable runnable, ContextView ctx) {
        Mono.fromRunnable(runnable)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }
}
