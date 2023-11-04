package ru.blc.cutlet.telegram.p.command;

import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.blc.cutlet.api.bot.Bot;
import ru.blc.cutlet.api.command.Messenger;
import ru.blc.cutlet.api.command.sender.CommandSender;
import ru.blc.cutlet.api.command.sender.DialogType;
import ru.blc.cutlet.telegram.p.TelegramBot;
import ru.blc.cutlet.telegram.p.TelegramModule;

import java.io.IOException;
import java.util.Objects;

@FieldDefaults(level = AccessLevel.PROTECTED)
public class SimpleCommandSender implements TelegramCommandSender {

    @Getter
    Update update;

    TelegramBot bot;


    @Getter
    long chatId;
    @Getter
    long userId;

    @Getter
    DialogType dialogType;

    public SimpleCommandSender(Update update, TelegramBot bot) {
        this.update = update;
        Chat chat;
        if (update.message() == null) {
            chat = update.callbackQuery().message().chat();
            this.userId = update.callbackQuery().from().id();
        } else {
            chat = update.message().chat();
            this.userId = update.message().from().id();
        }
        this.chatId = chat.id();
        this.bot = bot;
        this.dialogType = switch (chat.type()) {
            case Private -> DialogType.PRIVATE_MESSAGE;
            case group, supergroup, channel -> DialogType.CONVERSATION;
        };
    }

    @Override
    public boolean hasPermission(String permission) {
        bot.getLogger().warn("Did you does not override Simple Command Sender in your telegram bot?");
        return false;
    }

    @Override
    public CommandSender getPmSender() {
        return null;
    }

    @Override
    public String getName() {
        return "TG Simple Command Sender; " + update.message().from().id();
    }

    @Override
    public void sendMessage(String message) {
        sendMessage0(escapeFormatting(message));
    }

    @Override
    public void sendFormattedMessage(String message) {
        sendMessage0(message);
    }

    @Override
    public void sendAndDeleteMessage(String message) {
        sendMessage0(escapeFormatting(message));
    }

    @Override
    public void sendAndDeleteFormattedMessage(String message) {
        sendMessage0(message);
    }


    @Override
    public void sendMessage(Object message) {
        sendMessage0(message);
    }

    @Override
    public void sendAndDeleteMessage(Object message) {
        sendMessage0(message);
    }

    private void sendMessage0(String message) {
        bot.getLogger().debug("Sending message {} from bot {} for sender {} to chat {}", message, bot.getName(), this, chatId);
        SendMessage request = new SendMessage(chatId, message);
        sendMessage0(request);
    }

    private void sendMessage0(Object message) {
        if (message instanceof SendMessage request) {
            sendMessageRequest(request
                    .parseMode(ParseMode.MarkdownV2)
                    .disableWebPagePreview(true));
            return;
        }
        sendMessage0(String.valueOf(message));
    }

    private void sendMessageRequest(SendMessage request) {
        String text = (String) request.getParameters().get("text");
        if (text.length() > 3000) {
            String[] msgs = text.split("(?<=\\G(?s).{2800,3000}\\n)");
            bot.getLogger().debug("message split to {} parts", msgs.length);
            sendMessageSplit(msgs, request, 0);
            return;
        }
        sendMessageSplit(new String[]{text}, request, 0);
    }

    private void sendMessageSplit(String[] messagesSplit, SendMessage baseRequest, int index) {
        if (index >= messagesSplit.length) return;

        baseRequest.getParameters().put("text", messagesSplit[index]);
        bot.getApiBot().execute(baseRequest, new Callback<SendMessage, SendResponse>() {
            @Override
            public void onResponse(SendMessage request, SendResponse response) {
                bot.getLogger().debug("Answer for message is {}", response);
                sendMessageSplit(messagesSplit, baseRequest, index + 1);
            }

            @Override
            public void onFailure(SendMessage request, IOException e) {
                bot.getLogger().error("Error while send message ", e);
            }
        });
    }

    @Override
    public Messenger getMessenger() {
        return TelegramModule.TG_MESSENGER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleCommandSender that = (SimpleCommandSender) o;
        return chatId == that.chatId && userId == that.userId;
    }

    @Override
    public Bot getBot() {
        return (Bot) bot;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, userId);
    }

    @Override
    public void setDeleteIfPM(boolean deleteIfPM) {
        //noop via not supported by telegram bots
    }

    @Override
    public boolean isDeleteIfPM() {
        return false;
    }
}
