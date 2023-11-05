package ru.blc.cutlet.telegram.p;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import ru.blc.cutlet.api.Cutlet;
import ru.blc.cutlet.api.event.Event;
import ru.blc.cutlet.telegram.p.bean.CallbackButton;
import ru.blc.cutlet.telegram.p.event.chat.MessageNewEvent;
import ru.blc.cutlet.telegram.p.event.chat.member.LeftChatMemberEvent;
import ru.blc.cutlet.telegram.p.event.chat.member.NewChatMemberEvent;

import java.util.List;
import java.util.Optional;

public class UpdateListener implements UpdatesListener {

    protected final TelegramBot bot;

    public UpdateListener(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            try {
                process(update);
            } catch (Exception e) {
                bot.getLogger().error("Failed to process update " + update, e);
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    protected void process(Update update) {
        bot.getLogger().debug("processing update {}", update);
        if (update.message() != null) {
            Message message = update.message();
            if (message.entities() != null || message.text() != null) processNewTextMessage(update);
            processNewChatMembers(update);
            processLeftChatMember(update);
        }
        if (update.callbackQuery() != null) {
            processCallbackQuery(update);
        }
    }

    protected void processCallbackQuery(Update update) {
        CallbackQuery query = update.callbackQuery();
        if (query == null) return;
        bot.getLogger().debug("processing callback query {}", query);
        long chatId = Optional.of(query)
                .map(CallbackQuery::message)
                .map(Message::chat)
                .map(Chat::id)
                .orElse(0L);
        if (chatId == 0) {
            answerCallbackEmpty(query);
            return;
        }
        //seconds
        int date = Optional.of(query)
                .map(CallbackQuery::message)
                .map(Message::date)
                .orElse(0);
        if (System.currentTimeMillis() / 1000 > date + 10 * 60) { //10 min
            bot.getLogger().debug("Declined query via msg is too old. Msg {}, now {}", date, System.currentTimeMillis() / 1000);
            answerCallbackEmpty(query);
            return;
        }
        long userId = Optional.of(query)
                .map(CallbackQuery::from)
                .map(User::id)
                .orElse(0L);
        if (userId == 0) {
            answerCallbackEmpty(query);
            return;
        }
        String data = query.data();
        if (data == null || data.isBlank() || !data.matches("\\d+")) {
            answerCallbackEmpty(query);
            return;
        }
        long buttonId;
        try {
            buttonId = Long.parseLong(data);
        } catch (NumberFormatException e) {
            answerCallbackEmpty(query);
            return;
        }
        CallbackButton button = CallbackButton.getButton(buttonId);
        if (button == null) {
            answerCallbackEmpty(query);
            return;
        }
        try {
            if (!button.onPress(bot.getCommandSender(update), query.id(), bot.getLogger())) {
                answerCallbackEmpty(query);
            }
        } catch (Throwable t) {
            bot.getLogger().error("Error while processing callback query", t);
            answerCallbackEmpty(query);
        }
    }

    protected void answerCallbackEmpty(CallbackQuery query) {
        bot.getApiBot().execute(new AnswerCallbackQuery(query.id()));
    }

    protected void processNewTextMessage(Update update) {
        Message message = update.message();
        bot.getLogger().debug("processing new message {}", message);
        if (message.entities() != null)
            for (MessageEntity entity : message.entities()) {
                if (entity.type() == MessageEntity.Type.bot_command) {
                    bot.getLogger().debug("found command entity {}, process command", entity);
                    Cutlet.instance().dispatchCommand(message.text().substring(1),
                            bot.getCommandSender(update));
                    return;
                }
            }
        if (message.text() == null) {
            return;
        }
        if (message.text().startsWith(bot.getCommandsPrefix())) {
            bot.getLogger().debug("found command prefix {}, process command", message.text());
            Cutlet.instance().dispatchCommand(message.text().replaceFirst(bot.getCommandsPrefix(), ""),
                    bot.getCommandSender(update));
            return;
        }
        bot.getLogger().debug("command not found, just message");
        fire(new MessageNewEvent(update, bot.getCommandSender(update)));
    }

    protected void processNewChatMembers(Update update) {
        Message message = update.message();
        if (message == null) return;
        User[] users = message.newChatMembers();
        if (users == null) {
            return;
        }
        for (User user : users) {
            fire(new NewChatMemberEvent(user, update));
        }
    }

    protected void processLeftChatMember(Update update) {
        Message message = update.message();
        if (message == null) return;
        User user = message.leftChatMember();
        if (user == null) {
            return;
        }
        fire(new LeftChatMemberEvent(user, update));
    }

    protected void fire(Event event) {
        Cutlet.instance().getBotManager().callEvent(event, b -> b instanceof TelegramBot tg && tg == bot);
    }
}
