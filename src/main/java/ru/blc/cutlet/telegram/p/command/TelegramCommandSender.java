package ru.blc.cutlet.telegram.p.command;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.MessageEntity;
import com.pengrad.telegrambot.model.Update;
import ru.blc.cutlet.api.bean.ChatUser;
import ru.blc.cutlet.api.command.sender.CommandSender;
import ru.blc.cutlet.api.command.sender.TargetSearchResult;
import ru.blc.cutlet.telegram.p.TelegramModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface TelegramCommandSender extends CommandSender {

    Update getUpdate();

    long getUserId();

    long getChatId();

    default ChatUser getUserSender() {
        return new ChatUser(TelegramModule.TG_MESSENGER, getUserId());
    }

    @Override
    default List<TargetSearchResult> extractTargets(TargetSearchResult.FindCase... filter) {
        List<TargetSearchResult> targets = new ArrayList<>();
        try {
            targets.addAll(extractFromForward());
            targets.addAll(extractTagged());
        } catch (Throwable ignored) {
        }
        Set<TargetSearchResult.FindCase> filters = Set.of(filter);
        if (filters.isEmpty()) return targets;
        return targets.stream()
                .filter(t -> filters.contains(t.findCase()))
                .collect(Collectors.toList());
    }

    default List<TargetSearchResult> extractFromForward() {
        List<TargetSearchResult> targets = new ArrayList<>();

        Optional.ofNullable(getUpdate())
                .map(Update::message)
                .map(Message::forwardFrom)
                .ifPresent(u -> targets.add(new TargetSearchResult()
                        .target(new ChatUser(TelegramModule.TG_MESSENGER, u.id()))
                        .findCase(TargetSearchResult.FindCase.FORWARD))
                );

        Optional.ofNullable(getUpdate())
                .map(Update::message)
                .map(Message::replyToMessage)
                .map(Message::from)
                .ifPresent(u -> targets.add(new TargetSearchResult()
                        .target(new ChatUser(TelegramModule.TG_MESSENGER, u.id()))
                        .findCase(TargetSearchResult.FindCase.FORWARD))
                );

        return targets;
    }

    default List<TargetSearchResult> extractTagged() {
        List<TargetSearchResult> targets = new ArrayList<>();

        Optional.ofNullable(getUpdate())
                .map(Update::message)
                .map(Message::entities).stream().flatMap(Stream::of)
                .filter(e -> e.type() == MessageEntity.Type.mention || e.type() == MessageEntity.Type.text_mention)
                .filter(e -> e.user() != null)
                .forEach(e -> targets.add(new TargetSearchResult()
                        .target(new ChatUser(TelegramModule.TG_MESSENGER, e.user().id()))
                        .findCase(TargetSearchResult.FindCase.MENTION)
                        .startIndex(e.offset())
                        .endIndex(e.offset() + e.length())
                        .targetText(getUpdate().message().text().substring(e.offset(), e.offset() + e.length())))
                );

        return targets;
    }
}
