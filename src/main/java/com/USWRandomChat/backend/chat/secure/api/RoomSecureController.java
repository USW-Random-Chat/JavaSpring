package com.USWRandomChat.backend.chat.secure.api;

import com.USWRandomChat.backend.chat.secure.service.RoomSecureService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class RoomSecureController {

    private final RoomSecureService roomSecureService;

    //매칭 요청 처리
    @MessageMapping("/match")
    @SendToUser("/queue/match")
    public void handleMatchRequest(Principal principal) {
        String account = principal.getName();
        roomSecureService.addToMatchingQueue(account);
    }

    //매칭 취소 처리
    @MessageMapping("/cancel")
    @SendToUser("/queue/match")
    public void handleCancelRequest(Principal principal) {
        String account = principal.getName();
        roomSecureService.removeCancelParticipants(account);
    }
}
