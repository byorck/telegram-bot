package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.NotificationReminderService;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;

    private final NotificationReminderService notificationReminderService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationReminderService notificationReminderService) {
        this.telegramBot = telegramBot;
        this.notificationReminderService = notificationReminderService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            if (update.message() != null && update.message().text() != null) {
                String messageText = update.message().text();
                Long chatId = update.message().chat().id();

                if (messageText.equals("/start")) {
                    String welcomeMessage = "Добро пожаловать в бот-напоминатель. Введите напоминание в формате: dd.MM.yyyy HH:mm текст напоминания";
                    telegramBot.execute(new SendMessage(chatId, welcomeMessage));
                } else {
                    notificationReminderService.messageCatcher(messageText, chatId);
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
