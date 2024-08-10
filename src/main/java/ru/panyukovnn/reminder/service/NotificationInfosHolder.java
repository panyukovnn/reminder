package ru.panyukovnn.reminder.service;

import org.springframework.stereotype.Component;
import ru.panyukovnn.reminder.dto.NotificationInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class NotificationInfosHolder {

    private AtomicReference<List<NotificationInfo>> notificationInfosRef = new AtomicReference<>(new ArrayList<>());
}
