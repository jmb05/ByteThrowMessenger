package net.jmb19905.bytethrow.client.gtk;

import ch.bailu.gtk.adw.Application;
import ch.bailu.gtk.adw.ApplicationWindow;
import ch.bailu.gtk.gdk.Display;
import ch.bailu.gtk.gio.ApplicationFlags;
import ch.bailu.gtk.adw.*;
import ch.bailu.gtk.gtk.*;
import ch.bailu.gtk.lib.bridge.ListIndex;
import ch.bailu.gtk.lib.bridge.UiBuilder;
import ch.bailu.gtk.type.Strs;
import ch.bailu.gtk.type.exception.AllocationError;
import net.jmb19905.bytethrow.common.util.ResourceUtility;
import net.jmb19905.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class GuiManager {

    private final ApplicationWindow window;
    private final ListView chatList;
    private final TextView textView;
    private final Entry inputField;
    private final ActionBar actionBar;
    private boolean firstText = true;

    public GuiManager(Application app) {
        var xml = ResourceUtility.readWhole("window_layout.ui");
        UiBuilder builder = null;
        try {
            builder = UiBuilder.fromString(xml);
        } catch (AllocationError e) {
            Logger.error(e);
        }
        window = new ApplicationWindow(builder.getObject("window"));
        textView = new TextView(builder.getObject("text_view"));
        inputField = new Entry(builder.getObject("input_field"));
        actionBar = new ActionBar(builder.getObject("action_bar"));
        chatList = new ListView(builder.getObject("chat_list"));

        var listIndex = new ListIndex();
        List<ChatItem> strings = new ArrayList<>();
        strings.add(new ChatItem("Fred", true));
        strings.add(new ChatItem("Engelbert", false));
        listIndex.setSize(strings.size());

        var factory = new SignalListItemFactory();
        factory.onSetup(item -> {
            var listItem = new ListItem(item.cast());
            var label = new Label("invalid");
            label.addCssClass("list_item");
            label.setHexpand(true);
            listItem.setChild(label);
        });

        factory.onBind(item -> {
            var listItem = new ListItem(item.cast());
            var label = new Label(listItem.getChild().cast());
            var idx = ListIndex.toIndex(listItem);
            var str = strings.get(idx).name;
            label.setLabel(str);
        });

        chatList.setFactory(factory);
        chatList.setModel(listIndex.inSelectionModel());

        inputField.onActivate(() -> {
            var buffer = inputField.getBuffer();
            appendMessage(buffer.getText().toString(), !firstText);
            buffer.deleteText(0, buffer.getLength());
            firstText = false;
        });
        window.setApplication(app);
        window.show();
    }

    public void appendMessage(String message, boolean leadingNewLine) {
        var buffer = textView.getBuffer();
        var textIter = new TextIter();
        if (leadingNewLine) {
            buffer.getEndIter(textIter);
            buffer.insert(textIter, "\n", 1);
        }
        buffer.getEndIter(textIter);
        buffer.insertMarkup(textIter, message, message.length());

    }

    public static class ChatItem {
        private String name;
        private boolean online;
        public ChatItem(String name, boolean online) {
            this.name = name;
            this.online = online;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isOnline() {
            return online;
        }

        public void setOnline(boolean online) {
            this.online = online;
        }
    }

    public static void main(String[] args) {
        var app = new Application("net.jmb19905.bytethrow",
                ApplicationFlags.FLAGS_NONE);

        app.onStartup(() -> {
            var provider = new CssProvider();
            var css = ResourceUtility.readWhole("style.css");
            provider.loadFromData(css, css.length());
            StyleContext.addProviderForDisplay(
                    Display.getDefault(),
                    provider.asStyleProvider(),
                    GtkConstants.STYLE_PROVIDER_PRIORITY_APPLICATION
            );
        });

        app.onActivate(() -> {
            GuiManager manager = new GuiManager(app);
        });
        var result = app.run(args.length, new Strs(args));
        System.exit(result);
    }

    private static void buildUi(Application application) {
        var window = new ApplicationWindow(application);
    }

}
