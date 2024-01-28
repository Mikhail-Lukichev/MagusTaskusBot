package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.Task;
import pro.sky.telegrambot.repository.TaskRepository;

import javax.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.pengrad.telegrambot.model.request.ParseMode.HTML;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private TaskRepository taskRepository;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            // Process updates

            // listen for notification message
            Optional<Task> task = extractTaskFromUpdate(update);
            if (task.isPresent()) {

                LocalDateTime now = LocalDateTime.now();
                if (task.get().getNotificationTime().isAfter(now)) {
                    taskRepository.save(task.get());
                    logger.info("Task saved: {}", task.get());
                    sendTaskSetupResponse(task.get());
                } else {
                    //notification time is in the past. Do not save task
                    sendTaskInThePastResponse(task.get());
                }

            } else if (update.message().text().equals("/start")) {
                sendStartUpdateResponse(update);
            } else {
                sendUnrecognizedMessageResponse(update);
            }

        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendScheduledNotifications() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        System.out.println("now = " + now);
        List<Task> foundTasks = taskRepository.findByNotificationTime(now);
        foundTasks.forEach(this::sendNotification);
    }

    public Optional<Task> extractTaskFromUpdate(Update update) {
        Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16}) (.+)");
        Matcher matcher = pattern.matcher(update.message().text());
        if (matcher.matches()) {
            String dateTime = matcher.group(1);
            String msg = matcher.group(2);

            LocalDateTime notificationTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("dd.MM.yyy HH:mm"));
            Task task = new Task((Long) update.message().chat().id(), msg, notificationTime);

            return Optional.ofNullable(task);

        } else {
            return Optional.empty();
        }
    }

    public SendResponse sendNotification(Task task) {
        SendMessage sendMessage = new SendMessage(task.getChatId(), task.getMessage());
        return telegramBot.execute(sendMessage);
    }

    public SendResponse sendStartUpdateResponse(Update update) {
        String msg = "<b>Notification setup</b>\n" +
                "Send message in the following format:\n" +
                "DD.MM.YYYY HH:MM Message\n" +
                "<i>Example:</i> <code>15.01.2024 20:00 Submit homework</code>";
        SendMessage message = new SendMessage(update.message().chat().id(), msg);
        message.parseMode(HTML);
        return telegramBot.execute(message);
    }

    public SendResponse sendTaskSetupResponse(Task task) {
        String msg = "Notification with text \"" + task.getMessage() + "\" will be sent at " + task.getNotificationTime().format(DateTimeFormatter.ofPattern("HH:mm dd.MM.yyy"));
        SendMessage sendMessage = new SendMessage(task.getChatId(), msg);
        return telegramBot.execute(sendMessage);
    }

    public SendResponse sendTaskInThePastResponse(Task task) {
        String msg =    "<b>Notification time is in the past.</b>\n" +
                        "Notification will not be sent.";
        SendMessage sendMessage = new SendMessage(task.getChatId(), msg);
        sendMessage.parseMode(HTML);
        return telegramBot.execute(sendMessage);
    }

    public SendResponse sendUnrecognizedMessageResponse(Update update){
        String msg =    "<b>Message has not been recognized.</b>\n" +
                        "Please check the format:\n" +
                        "DD.MM.YYYY HH:MM Message\n" +
                        "<i>Example:</i> <code>15.01.2024 20:00 Submit homework</code>";
        SendMessage sendMessage = new SendMessage(update.message().chat().id(), msg);
        sendMessage.parseMode(HTML);
        return telegramBot.execute(sendMessage);
    }

}
