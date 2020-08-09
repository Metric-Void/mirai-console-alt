package com.metricv.mirai.router;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.message.FriendMessageEvent;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.message.TempMessageEvent;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageSource;

import java.util.HashMap;

/**
 * The result of a routing.
 */
public class RoutingResult extends HashMap<Object, Object> {
    protected MessageSource msgSource;
    protected Event eventSource;

    public boolean isGroupMsg() {
        return eventSource instanceof GroupMessageEvent;
    }

    /**
     * Get the original event, whatever it is.
     * @return The original event in the fashion of an {@link Event} Object.
     */
    public Event getEvent() {
        return eventSource;
    }

    /**
     * Try getting the original event as a group event.
     * @return {@link GroupMessageEvent} of original event, or null.
     */
    public GroupMessageEvent getGroupEventSource() {
        return (eventSource instanceof GroupMessageEvent)? (GroupMessageEvent) eventSource : null;
    }

    /**
     * Just reply to the original chat window, whether it is group or private.
     * @param msg A plain string message.
     */
    public void sendMessage(String msg) {
        if(eventSource instanceof GroupMessageEvent) {
            ((GroupMessageEvent) eventSource).getGroup().sendMessage(msg);
        } else if (eventSource instanceof FriendMessageEvent) {
            ((FriendMessageEvent) eventSource).getSender().sendMessage(msg);
        } else if (eventSource instanceof TempMessageEvent) {
            ((TempMessageEvent) eventSource).getSender().sendMessage(msg);
        }
    }

    /**
     * Just reply to the original chat window, whether it is group or private.
     * @param msg A constructed {@link Message}. You can also send a {@link MessageChain}.
     */
    public void sendMessage(Message msg) {
        if(eventSource instanceof GroupMessageEvent) {
            ((GroupMessageEvent) eventSource).getGroup().sendMessage(msg);
        } else if (eventSource instanceof FriendMessageEvent) {
            ((FriendMessageEvent) eventSource).getSender().sendMessage(msg);
        } else if (eventSource instanceof TempMessageEvent) {
            ((TempMessageEvent) eventSource).getSender().sendMessage(msg);
        }
    }

    public RoutingResult() {
        super();
        msgSource = null;
    }

    public MessageSource getMsgSource() {
        return msgSource;
    }

    public void insertNonNamed(Object matchResult) {
        this.put(this.size() + 1, matchResult);
    }
}
