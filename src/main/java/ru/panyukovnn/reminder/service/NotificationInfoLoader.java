package ru.panyukovnn.reminder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.panyukovnn.reminder.dto.GithubFileInfo;
import ru.panyukovnn.reminder.dto.NotificationInfo;
import ru.panyukovnn.reminder.property.ReminderProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationInfoLoader {

    private static final AtomicReference<List<? extends ScheduledFuture<?>>> scheduledTasks = new AtomicReference<>(new ArrayList<>());

    @Autowired
    private WebClient webClient;
    @Autowired
    private Validator validator;
    @Autowired
    private TaskScheduler executor;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ReminderProperty reminderProperty;
    @Autowired
    private ReminderManager reminderManager;

    @EventListener(ApplicationStartedEvent.class)
    public void loadOnStartup() {
        loadNotifications()
                .subscribe();
    }

    public Mono<Void> loadNotifications() {
        return downloadNotificationInfosFromGithub()
                .mapNotNull(rawNotificationInfo -> readNotificationInfo(rawNotificationInfo).orElse(null))
                .doOnNext(notificationInfo -> log.info("Загружено уведомление из репозитория: {}", notificationInfo.getName()))
                .filter(this::filterNotificationInfo)
                .map(notificationInfo -> {
                    log.info("Успешно добавлено напоминание: {}", notificationInfo.getName());

                    return executor.schedule(
                            () -> reminderManager.processNotificationInfo(false, false, notificationInfo),
                            new CronTrigger(notificationInfo.getCron()));
                })
                .collect(Collectors.toList())
                .doOnNext(list -> {
                    List<? extends ScheduledFuture<?>> previousScheduledFutures = scheduledTasks.get();
                    scheduledTasks.set(list);
                    previousScheduledFutures.forEach(it -> it.cancel(true));
                })
                .then();
    }

    private boolean filterNotificationInfo(NotificationInfo notificationInfo) {
        Set<ConstraintViolation<NotificationInfo>> constraintViolations = validator.validate(notificationInfo);

        if (!CollectionUtils.isEmpty(constraintViolations)) {
            constraintViolations.forEach(cv -> log.warn("Ошибка валидации уведомления '{}': {}", notificationInfo.getName(), cv.getMessage()));

            return false;
        }

        if (!notificationInfo.isActive()) {
            log.info("Уведомление '{}' неактивно", notificationInfo.getName());

            return false;
        }

        return true;
    }

    private Optional<NotificationInfo> readNotificationInfo(String rawNotificationInfo) {
        try {
            return Optional.of(objectMapper.readValue(rawNotificationInfo, NotificationInfo.class));
        } catch (IOException e) {
            log.error("Не удалось прочитать файл с информацией об уведомлении: {}", e.getMessage(), e);

            return Optional.empty();
        }
    }

    private Flux<String> downloadNotificationInfosFromGithub() {
        String uri = UriComponentsBuilder.fromHttpUrl("https://api.github.com/repos/PanyukovNN/project-configs/contents/")
            .build()
            .toUri()
            .toString();

        Flux<GithubFileInfo> githubFileInfosFlux = fetchFolderFiles(uri + reminderProperty.getNotificationConfigPath())
            .flatMap(it -> {
                if (it.getType().equals("dir")) {
                    // Рекурсивно извлекаем файлы из папок
                    return fetchFolderFiles(uri + it.getPath());
                }

                return Flux.just(it);
            });

        return githubFileInfosFlux
                .flatMap(githubFileInfo -> webClient.get()
                        .uri(githubFileInfo.getDownloadUrl())
                        .retrieve()
                        .bodyToMono(String.class));
    }

    private Flux<GithubFileInfo> fetchFolderFiles(String uri) {
        return webClient.get()
            .uri(uri)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + reminderProperty.getGithubProjectConfigsToken())
            .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<GithubFileInfo>>() {
            })
            .flatMapMany(Flux::fromIterable);
    }
}
