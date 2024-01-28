package pro.sky.telegrambot.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long chatId;

    private String message;

    private LocalDateTime notificationTime;

    public Task() {
    }

    public Task(Long chatId, String message, LocalDateTime notificationTime) {
        this.chatId = chatId;
        this.message = message;
        this.notificationTime = notificationTime;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", message='" + message + '\'' +
                ", notificationTime=" + notificationTime +
                '}';
    }

    public Long getId() {
        return id;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getNotificationTime() {
        return notificationTime;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setNotificationTime(LocalDateTime notificationTime) {
        this.notificationTime = notificationTime;
    }
}
