package ru.blc.cutlet.telegram.p;

import ru.blc.cutlet.api.command.Messenger;
import ru.blc.cutlet.api.module.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TelegramModule extends Module {

    public static final Messenger TG_MESSENGER = new TelegramMessenger();

    private final List<TelegramBot> connectedBots = new CopyOnWriteArrayList<>();

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {
        for (TelegramBot connectedBot : new ArrayList<>(connectedBots)) {
            disconnect(connectedBot);
        }
    }

    public void connect(TelegramBot bot) {
        if (!connectedBots.contains(bot)) {
            bot.getApiBot().setUpdatesListener(new UpdateListener(bot));
            connectedBots.add(bot);
        }
    }

    public void disconnect(TelegramBot bot) {
        if (connectedBots.contains(bot)) {
            connectedBots.remove(bot);
            bot.getApiBot().removeGetUpdatesListener();
            bot.getApiBot().shutdown();
        }
    }
}
