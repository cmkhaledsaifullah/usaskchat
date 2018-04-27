package com.srlab.chatapplication.Model;

import java.util.List;

public class ChatRoom
{
    String roomid;
    List<Message> messages;

    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
