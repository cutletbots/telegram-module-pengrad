package ru.blc.cutlet.telegram.p;

import com.pengrad.telegrambot.model.Update;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import ru.blc.cutlet.telegram.p.command.SimpleCommandSender;
import ru.blc.cutlet.telegram.p.command.TelegramCommandSender;

public interface TelegramBot {

    String getName();

    Logger getLogger();

    @NotNull
    com.pengrad.telegrambot.TelegramBot getApiBot();

    @Nullable
    default UpdateListener getUpdateListener() {
        return null;
    }

    /**
     * Последовательность символов, с которых начинается команда. По умолчанию "/"
     *
     * @return префикс команд
     */
    default String getCommandsPrefix() {
        return "/";
    }

    default TelegramCommandSender getCommandSender(Update update) {
        return new SimpleCommandSender(update, this);
    }
}
