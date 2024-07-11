package com.USWRandomChat.backend.chat.secure.service;

import com.USWRandomChat.backend.chat.domain.Message;
import com.USWRandomChat.backend.chat.dto.MessageRequest;
import com.USWRandomChat.backend.chat.repository.MessageRepository;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSecureService {

    private final ProfileRepository profileRepository;
    private final MessageRepository messageRepository;
    //@Resource(name = "redisTemplateMessage")
    private RedisTemplate<String, Message> redisTemplateMessage;
    //@Resource(name = "redisTemplateMessage")
    //private final RedisTemplate<String, com.USWRandomChat.backend.chat.domain.Message> redisTemplate;
//    @Autowired
//    private static RedisTemplate<String,Message>redisTemplate;
//    @Autowired
//    private static final RedisTemplate<String,Message>redisTemplate = new RedisTemplate<>();

    private static final String MESSAGE_CACHE_KEY = "messageCacheRoom:";

    //채팅방 찾기
    public Profile findRoom(String roomId) {
        Profile profile = findExistRoom(roomId);

        return profile;
    }

    // 채팅방 존재 검증
    private Profile findExistRoom(String roomId) {
        Optional<Profile> optionalProfile = profileRepository.findByRoomId(roomId);

        return optionalProfile.orElse(null);
    }

//    public Page<MessageRequest> findMessages123(String roomId, int page, int size) {
//        Profile profileRoomId = findRoom(roomId);
//
//        Pageable pageable = PageRequest.of(page-1, size, Sort.by("messageNumber").descending());
//        Page<MessageRequest> messages = messageRepository.findByRoomId(pageable, profileRoomId);
//
//        return messages;
//    }
    @Transactional(readOnly = true)
    public Page<Message> findMessages(String roomId, int page, int size){
        String cacheKey = MESSAGE_CACHE_KEY+roomId;
        long start = (page -1) * size;
        long end = start + size -1;

        List<Message> cachedMessages = redisTemplateMessage.opsForList().range(cacheKey, start, end);
        Profile profileRoomId = findRoom(roomId);

        List<Message> dbMessages = new ArrayList<>();

        if(cachedMessages.size() < size) {
            // DB에서 가져와야 할 페이지 수
            int dbPage = page - cachedMessages.size()/size;
            Pageable pageable = PageRequest.of(dbPage, size - cachedMessages.size());
            dbMessages = messageRepository.findAllByRoomIdOrderBySendTimeDesc(profileRoomId, pageable).getContent();
        }

        List<Message> allMessages=new ArrayList<>();
        allMessages.addAll(cachedMessages);
        allMessages.addAll(dbMessages);
        Collections.sort(allMessages, Comparator.comparing(Message::getSendTime));

        int totalElements = allMessages.size();
        int totalPage = (int) Math.ceil(totalElements/size);
        int startIndex = (page -1) * size;
        int endIndex = Math.min(startIndex + size, totalElements);

        List<Message> pageMessages = allMessages.subList(startIndex,endIndex);
        return new PageImpl<>(pageMessages, PageRequest.of(page,size),totalPage);

    }


//    public void saveMessage(MessageRequest messageRequest, String roomId) {
//
//        /*
//        * 채팅방에 따라 독립적인 messageNumber 증가
//        * */
//        int lastMessageNumber = messageRepository.findTopByRoomIdOrderByMessageNumberDesc(roomId)
//                .map(Message::getMessageNumber)
//                .orElse(0);
//
//        //messageNumber 증가 후 메시지 저장
//        Message message = Message
//                .builder()
//                .roomId(roomId)
//                .messageNumber(lastMessageNumber + 1)
//                .sender(messageRequest.getSender())
//                .contents(messageRequest.getContents())
//                .sendTime(LocalDateTime.now())
//                .build();
//
//        messageRepository.save(message);
//        log.info("메시지 저장");
//    }

    //레디스에 메시지 캐싱
    public void CachedMessage(MessageRequest dto, Long roomId){


        Message message= Message.builder()
                .contents(dto.getContents())
                .roomId(dto.getRoomId())
                .sender(dto.getSender())
                .sendTime(LocalDateTime.now())
                .build();
        String cacheKey=MESSAGE_CACHE_KEY+roomId;

        redisTemplateMessage.opsForList().rightPush(cacheKey, message);
    }

    @Scheduled(cron="0 0 0/1 * * *")//한시간마다
    @Transactional
    public void saveMessages(){
        //레디스에 캐싱된 채팅방 아이디만 파싱
        List<Long>roomIdList=redisTemplateMessage.keys(MESSAGE_CACHE_KEY+'*').stream()
                .map(key -> Long.parseLong(key.substring(MESSAGE_CACHE_KEY.length())))
                .collect(Collectors.toList());
        //각 채팅방의 캐싱된 메세지를 찾아 데이터베이스에 저장한 후, 캐싱된 메세지는 삭제
        for(Long id : roomIdList){
            String cacheKey=MESSAGE_CACHE_KEY+id;
            try{
                List<Message>messages=redisTemplateMessage.opsForList().range(cacheKey,0,-1);
                if(messages != null && messages.size()>0){

                    messageRepository.saveAll(messages);//채팅메시지를 데이터베이스에 저장
                    redisTemplateMessage.opsForList().trim(cacheKey, messages.size(), -1);//저장 후 해당 채팅 메시지를 캐시에서 삭제
                }else{
                    continue;
                }
            }catch (Exception e){
                log.error(e.getMessage());
            }
        }
    }


    @Transactional
    public void initMessage() {
        // 초기 데이터 준비
        Message message1 = Message.builder()
                .messageNumber(1)
                .roomId("room1")
                .sender("user1")
                .contents("Hello, this is a test message.")
                .sendTime(LocalDateTime.now())
                .build();

        Message message2 = Message.builder()
                .messageNumber(2)
                .roomId("room1")
                .sender("user2")
                .contents("Hi, this is another test message.")
                .sendTime(LocalDateTime.now())
                .build();

        // 데이터베이스에 저장
        messageRepository.save(message1);
        messageRepository.save(message2);
    }


}
