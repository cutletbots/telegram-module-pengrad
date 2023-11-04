package ru.blc.cutlet.telegram.p.event.chat;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import ru.blc.cutlet.telegram.p.event.TelegramEvent;

public abstract class MessageEvent extends TelegramEvent {

    protected final Message message;

    public MessageEvent(Update update) {
        super(update);
        this.message = update.message();
    }

    public Message getMessage() {
        return message;
    }
}
