package ru.blc.cutlet.telegram.p;

import ru.blc.cutlet.api.command.Messenger;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelegramMessenger implements Messenger {

    public static final Pattern TAG_PATTERN = Pattern.compile("\\[([^]]+)]\\(tg://user\\?id=(\\d+)\\)");
    private static final char ESCAPE_SYMBOL = '\\';
    private static final List<Character> BAD_SYMBOLS =
            List.of(ESCAPE_SYMBOL, '_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!');

    @Override
    public String escaped(String text) {
        Matcher m = TAG_PATTERN.matcher(text);
        boolean matchedOnce = false;
        int prevEnd = 0;
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            matchedOnce = true;
            if (prevEnd < m.start()) {
                sb.append(replaceSymbols(text.substring(prevEnd, m.start())));
            }
            prevEnd = m.end();
            sb.append(m.group(0).replace(m.group(1), replaceSymbols(m.group(1))));
        }
        if (!matchedOnce) {
            return replaceSymbols(text);
        } else if (prevEnd < text.length()) {
            sb.append(replaceSymbols(text.substring(prevEnd)));
        }
        return sb.toString();
    }

    protected String replaceSymbols(String text) {
        for (Character c : BAD_SYMBOLS) {
            text = text.replace(Character.toString(c), ESCAPE_SYMBOL + Character.toString(c));
        }
        return text;
    }


}
