package ru.blc.cutlet.telegram.p.event;

import com.pengrad.telegrambot.model.Update;
import ru.blc.cutlet.api.event.Event;

public abstract class TelegramEvent extends Event {

    protected final Update update;

    public TelegramEvent(Update update) {
        this.update = update;
    }

    public Update getUpdate() {
        return update;
    }
}
