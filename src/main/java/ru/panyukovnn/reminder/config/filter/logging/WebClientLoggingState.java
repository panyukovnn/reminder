package ru.panyukovnn.reminder.config.filter.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import reactor.util.context.ContextView;
import ru.panyukovnn.reminder.config.filter.WebFilterUtil;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.joining;

/**
 * Состояние логирования запроса/ответа.
 */
@Slf4j
@RequiredArgsConstructor
public class WebClientLoggingState {

    /**
     * Множество url, по которым необходимо пропустить проверки и в любом случае логгировать тело.
     */
    private static final Set<String> NEED_LOG_BODY_URLS = Set.of("https://xn----7sbonvkui.xn--p1ai/");

    private final UUID id = UUID.randomUUID();
    private final Instant from = Instant.now();

    private final String url;
    private final ContextView ctx;
    private final WebFilterUtil webFilterUtil;

    public void logRequest(String method, String url, HttpHeaders headers) {
        webFilterUtil.runAsync(
                () -> log.info("WebClient request [{}] {} {} {}", id, method, url, defineHeaders(headers)),
                ctx);
    }

    public void logRequestBody(DataBuffer dataBuffer, HttpHeaders headers) {
        MediaType contentType = headers.getContentType();
        if (contentType != null
                && !contentType.getType().contains(MediaType.APPLICATION_JSON_VALUE)) {
            return;
        }

        try (DataBuffer.ByteBufferIterator byteBufferIterator = dataBuffer.readableByteBuffers()) {
            byte[] bytes = new byte[dataBuffer.readableByteCount()];
            int offset = 0;

            while (byteBufferIterator.hasNext()) {
                ByteBuffer byteBuffer = byteBufferIterator.next();

                byteBuffer.get(bytes, offset, byteBuffer.remaining());
                offset += byteBuffer.remaining();
            }

            webFilterUtil.runAsync(() -> {
                        String requestBody = new String(bytes, StandardCharsets.UTF_8);

                        log.info("WebClient request body [{}] {}", id, requestBody);
                    },
                    ctx);
        } catch (RuntimeException e) {
            log.error("Ошибка при логировании тела запроса", e);
        }
    }

    public <T> void logResponse(T body, HttpStatusCode status, HttpHeaders headers) {
        webFilterUtil.runAsync(() -> {
                    try {
                        MediaType contentType = headers.getContentType();

                        boolean isSkipBoddyLoggingChecks = NEED_LOG_BODY_URLS.stream().anyMatch(url::contains);

                        String responseBody = !isSkipBoddyLoggingChecks && (contentType != null && contentType.isPresentIn(List.of(MediaType.TEXT_HTML)))
                                ? "html body ignored"
                                : webFilterUtil.readResponseBodyString(body);

                        log.info("WebClient response [{}] {} {} {} {} in {}ms", id, url, status.value(),
                                defineHeaders(headers), responseBody, Duration.between(from, Instant.now()).toMillis());
                    } catch (Exception e) {
                        log.error("Ошибка при логировании ответа", e);
                    }
                },
                ctx);
    }

    private String defineHeaders(HttpHeaders headers) {
        return headers.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(value -> String.format("[%s: %s]", entry.getKey(), value.replace("\n", " "))))
                .collect(joining(","));
    }
}