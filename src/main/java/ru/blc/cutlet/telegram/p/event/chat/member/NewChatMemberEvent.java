package ru.blc.cutlet.telegram.p.event.chat.member;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import lombok.Getter;
import ru.blc.cutlet.api.event.HandlerList;
import ru.blc.cutlet.telegram.p.event.UserEvent;

public class NewChatMemberEvent extends UserEvent {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Getter
    private final Chat chat;
    @Getter
    private final Message message;

    public NewChatMemberEvent(User user, Update update) {
        super(user, update);
        message = update.message();
        chat = update.message().chat();
    }
}
