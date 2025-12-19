package dev.xpple.seedmapper.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

public final class ChatBuilder {

    private ChatBuilder() {
    }

    private static final ChatFormatting BASE = ChatFormatting.GRAY;
    private static final ChatFormatting ACCENT = ChatFormatting.AQUA;
    private static final ChatFormatting HIGHLIGHT = ChatFormatting.WHITE;
    private static final ChatFormatting DARK = ChatFormatting.DARK_GRAY;
    private static final ChatFormatting WARN = ChatFormatting.YELLOW;
    private static final ChatFormatting ERROR = ChatFormatting.RED;

    public static MutableComponent chain(Component... components) {
        return chain(List.of(components));
    }

    public static MutableComponent chain(List<? extends Component> components) {
        MutableComponent result = Component.empty();
        components.forEach(result::append);
        return result;
    }

    public static MutableComponent join(Component delimiter, Component... components) {
        return join(delimiter, List.of(components));
    }

    public static MutableComponent join(Component delimiter, Stream<? extends Component> components) {
        return join(delimiter, components.toList());
    }

    public static MutableComponent join(Component delimiter, List<? extends Component> components) {
        MutableComponent result = Component.empty();
        if (components.isEmpty()) {
            return result;
        }
        if (components.size() == 1) {
            return result.append(components.getFirst());
        }
        for (int i = 0; i < components.size() - 1; i++) {
            result.append(components.get(i)).append(delimiter);
        }

        return result.append(components.getLast());
    }

    public static MutableComponent format(MutableComponent component, ChatFormatting... formatting) {
        return component.withStyle(style -> style.applyFormats(formatting));
    }

    public static MutableComponent component(String string) {
        return Component.literal(string);
    }

    public static MutableComponent highlight(String string) {
        return highlight(component(string));
    }

    public static MutableComponent base(String string) {
        return base(component(string));
    }

    public static MutableComponent accent(String string) {
        return accent(component(string));
    }

    public static MutableComponent dark(String string) {
        return dark(component(string));
    }

    public static MutableComponent error(String string) {
        return error(component(string));
    }

    public static MutableComponent warn(String string) {
        return warn(component(string));
    }

    public static MutableComponent highlight(MutableComponent component) {
        return format(component, HIGHLIGHT);
    }

    public static MutableComponent base(MutableComponent component) {
        return format(component, BASE);
    }

    public static MutableComponent accent(MutableComponent component) {
        return format(component, ACCENT);
    }

    public static MutableComponent dark(MutableComponent component) {
        return format(component, DARK);
    }

    public static MutableComponent error(MutableComponent component) {
        return format(component, ERROR);
    }

    public static MutableComponent warn(MutableComponent component) {
        return format(component, WARN);
    }

    public static MutableComponent hover(MutableComponent component, MutableComponent hover) {
        return component.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(hover)));
    }

    public static MutableComponent copy(MutableComponent component, String copy) {
        return component.withStyle(style -> style.withClickEvent(new ClickEvent.CopyToClipboard(copy)));
    }

    public static MutableComponent command(MutableComponent component, String command) {
        return component.withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand(command)));
    }

    public static MutableComponent file(MutableComponent component, String file) {
        return component.withStyle(style -> style.withClickEvent(new ClickEvent.OpenFile(file)));
    }

    public static MutableComponent url(MutableComponent component, URI url) {
        return component.withStyle(style -> style.withClickEvent(new ClickEvent.OpenUrl(url)));
    }

    public static MutableComponent page(MutableComponent component, int page) {
        return component.withStyle(style -> style.withClickEvent(new ClickEvent.ChangePage(page)));
    }

    public static MutableComponent run(MutableComponent component, Runnable runnable) {
        return page(component, RunnableClickEventActionHelper.registerCode(runnable));
    }

    public static MutableComponent suggest(MutableComponent component, String command) {
        return component.withStyle(style -> style.withClickEvent(new ClickEvent.SuggestCommand(command)));
    }
}
