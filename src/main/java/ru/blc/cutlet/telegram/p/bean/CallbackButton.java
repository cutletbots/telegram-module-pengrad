package ru.blc.cutlet.telegram.p.bean;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import ru.blc.cutlet.telegram.p.TelegramBot;
import ru.blc.cutlet.telegram.p.command.TelegramCommandSender;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

public class CallbackButton extends InlineKeyboardButton {

    private static final AtomicLong id = new AtomicLong(0L);
    private static final AtomicLong removeId = new AtomicLong(0L);
    private static final int MAX_CACHED_ACTIONS = 1000;
    private static final Long2ObjectMap<CallbackButton> cache = new Long2ObjectOpenHashMap<>();

    synchronized public static CallbackButton getButton(long id) {
        return cache.get(id);
    }

    public static InlineKeyboardButton create(String text, CallbackFunction onPress) {
        CallbackButton callbackButton = new CallbackButton(text, onPress);
        return new InlineKeyboardButton(text)
                .callbackData(Long.toString(callbackButton.ownId));
    }


    public static InlineKeyboardButton create(String text, Consumer<TelegramCommandSender> onPress) {
        CallbackFunction f = null;
        if (onPress != null) {
            f = (sn) -> {
                onPress.accept(sn);
                return null;
            };
        }
        return create(text, f);
    }

    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private long ownId;

    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private final CallbackFunction onPress;

    protected CallbackButton(String text, CallbackFunction onPress) {
        super(text);
        this.onPress = onPress;
        this.ownId = id.getAndAdd(1L);
        synchronized (cache) {
            cache.put(ownId, this);
            while (cache.size() > MAX_CACHED_ACTIONS) {
                cache.remove(removeId.getAndAdd(1L));
            }
        }
    }

    public boolean onPress(TelegramCommandSender sender, String queryId, Logger logger) {
        if (onPress != null) {
            try {
                String result = onPress.apply(sender);
                if (result != null) {
                    answerEvent(result, queryId, (TelegramBot) sender.getBot());
                    return true;
                }
            } catch (Exception e) {
                logger.error("Error while accepting button action", e);
            }
        }
        return false;
    }

    protected void answerEvent(@NotNull String text, String queryId, @NotNull TelegramBot bot) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(queryId);
        if (!text.isBlank()) {
            answerCallbackQuery.text(text)
                    .showAlert(false);
        }
        bot.getApiBot().execute(answerCallbackQuery);
    }

    public interface CallbackFunction extends Function<TelegramCommandSender, String> {

    }
}
