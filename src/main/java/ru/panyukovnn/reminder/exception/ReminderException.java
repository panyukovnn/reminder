package ru.panyukovnn.reminder.exception;

/**
 * Базовое исключение приложения.
 */
public class ReminderException extends RuntimeException {

    public ReminderException(String message) {
        super(message);
    }

    public ReminderException(String message, Exception e) {
        super(message, e);
    }
}
