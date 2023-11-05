package ru.blc.cutlet.telegram.p.event;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;

public abstract class UserEvent extends TelegramEvent {

    private final User user;

    public UserEvent(User user, Update update) {
        super(update);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
