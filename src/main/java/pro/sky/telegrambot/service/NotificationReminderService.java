package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.Notification;
import pro.sky.telegrambot.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationReminderService {
    private final NotificationRepository repository;

    private final TelegramBot telegramBot;

    private static final Pattern PATTERN = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public NotificationReminderService(NotificationRepository repository, TelegramBot telegramBot) {
        this.repository = repository;
        this.telegramBot = telegramBot;
    }

    public void messageCatcher(String message, Long chatId) {
        Matcher matcher = PATTERN.matcher(message);
        if (matcher.matches()) {
            String dateTimeString = matcher.group(1);
            String reminderText = matcher.group(3);
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);

            if (dateTime.isBefore(LocalDateTime.now())) {
                telegramBot.execute(new SendMessage(chatId, "Ошибка: дата и время напоминания не могут быть в прошлом"));
                return;
            }

            Notification notification = new Notification();
            notification.setChatId(chatId);
            notification.setNotificationText(reminderText);
            notification.setNotificationDatetime(dateTime);
            repository.save(notification);
            telegramBot.execute(new SendMessage(chatId, "Напоминание: \"" + reminderText + "\" сохранено под номером " + notification.getId()));
        } else {
            telegramBot.execute(new SendMessage(chatId, "Неверный формат напоминания: \"" + message + "\" Напоминание должно быть в формате: \"dd.MM.yyyy HH:mm текст напоминания\""));
        }
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void checkAndSendNotifications() {
        LocalDateTime currentMinute = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        List<Notification> tasksToNotify = repository.findByNotificationDatetime(currentMinute);

        tasksToNotify.forEach(task -> {
            telegramBot.execute(new SendMessage(task.getChatId(), "Напоминаю что сейчас запланировано: " + task.getNotificationText()));
            repository.deleteById(task.getId());
        });
    }
}