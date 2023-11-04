package ru.blc.cutlet.telegram.p.event.chat;

import com.pengrad.telegrambot.model.Update;
import ru.blc.cutlet.api.event.HandlerList;
import ru.blc.cutlet.telegram.p.command.TelegramCommandSender;

public class MessageNewEvent extends MessageEvent {

    private static final HandlerList handlers = new HandlerList();
    private final TelegramCommandSender sender;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public MessageNewEvent(Update update, TelegramCommandSender sender) {
        super(update);
        this.sender = sender;
    }

    public TelegramCommandSender getSender() {
        return sender;
    }
}
