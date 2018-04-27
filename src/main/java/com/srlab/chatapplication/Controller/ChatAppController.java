package com.srlab.chatapplication.Controller;

import com.srlab.chatapplication.Model.ChatRoom;
import com.srlab.chatapplication.Model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;


import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

@Controller
public class ChatAppController
{

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;


    List<ChatRoom> rooms;

    @Autowired
    public void ChatController()
    {
        rooms = new ArrayList<ChatRoom>();
    }

    @MessageMapping("/chat/{roomId}/sendMessage")
    public void sendMessage(@DestinationVariable String roomId, @Payload Message chatMessage) {
        addmessage(roomId,chatMessage);
        messagingTemplate.convertAndSend(format("/room/%s", roomId), chatMessage);
    }

    @MessageMapping("/chat/{roomId}/addUser")
    public void addUser(@DestinationVariable String roomId, @Payload Message chatMessage,SimpMessageHeaderAccessor headerAccessor) {
        String currentRoomId = (String) headerAccessor.getSessionAttributes().put("room_id", roomId);
        if (currentRoomId != null) {
            Message leaveMessage = new Message();
            leaveMessage.setType(Message.MessageType.LEAVE);
            leaveMessage.setSender(chatMessage.getSender());
            addmessage(currentRoomId,chatMessage);
            messagingTemplate.convertAndSend(format("/room/%s", currentRoomId), leaveMessage);
        }

        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        addmessage(roomId,chatMessage);
        messagingTemplate.convertAndSend(format("/room/%s", roomId), chatMessage);
    }


    @SubscribeMapping("/chat/rooms")
    public List<ChatRoom> listOfRoom()
    {
        //System.out.println("Rooms: "+rooms.size());
        return rooms;
    }

    @MessageMapping("/chat/rooms")
    public void addRoom(@Payload ChatRoom chatRoom)
    {
        String roomId = chatRoom.getRoomid();
        int flag=0;
        for(ChatRoom room:rooms)
        {
            if(room.getRoomid().equals(roomId))
            {
                flag = 1;
                break;
            }
        }
        if(flag==0)
        {
            List<Message> messages = new ArrayList<Message>();
            chatRoom.setMessages(messages);
            rooms.add(chatRoom);
        }


        messagingTemplate.convertAndSend("/room/list", rooms);
    }


    @MessageMapping("/chat/{roomId}/leaveuser")
    public void leaveRoom(@DestinationVariable String roomId, @Payload Message chatMessage,SimpMessageHeaderAccessor headerAccessor)
    {
        String currentRoomId = (String) headerAccessor.getSessionAttributes().put("room_id", roomId);
        if (currentRoomId != null) {
            Message leaveMessage = new Message();
            leaveMessage.setType(Message.MessageType.LEAVE);
            leaveMessage.setSender(chatMessage.getSender());
            addmessage(currentRoomId,chatMessage);
            messagingTemplate.convertAndSend(format("/room/%s", currentRoomId), leaveMessage);
        }
    }


    private void addmessage(String roomid, Message message)
    {
        for(ChatRoom room: rooms)
        {
            if(room.getRoomid().equals(roomid))
            {
                List<Message> messages = room.getMessages();
                messages.add(message);
                room.setMessages(messages);
                break;
            }
        }
    }

    @SubscribeMapping("chat/{roomId}/getPrevious")
    public List<Message> getPreviousMessages(@DestinationVariable String roomId)
    {
        System.out.println("Room Id is: "+roomId);
        List<Message> messages = new ArrayList<Message>();
        for(ChatRoom room: rooms)
        {
            if(room.getRoomid().equals(roomId))
            {
                messages = room.getMessages();
                break;
            }
        }
        return messages;
    }

}