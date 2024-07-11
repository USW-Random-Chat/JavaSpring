package com.USWRandomChat.backend.chat.mapper;

import com.USWRandomChat.backend.chat.domain.Message;
import com.USWRandomChat.backend.chat.domain.PubMessage;
import com.USWRandomChat.backend.chat.dto.MessageRequest;
import com.USWRandomChat.backend.chat.dto.MessageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {

//    @Mapping(target = "roomId", source = "roomId")
//    @Mapping(target = "sender", source = "sender")
//    @Mapping(target = "contents", source = "contents")
    PubMessage messageRequestToEntity(MessageRequest messageRequest);

    List<MessageResponse> messagesToMessageResponseDtos(List<Message> messages);
    
}
