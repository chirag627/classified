package com.classified.app.controller;

import com.classified.app.dto.request.SendMessageRequest;
import com.classified.app.dto.response.MessageResponse;
import com.classified.app.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Messaging endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    @Operation(summary = "Send a message")
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(messageService.sendMessage(request, userDetails.getUsername()));
    }

    @GetMapping("/conversation/{otherUserId}")
    @Operation(summary = "Get conversation with another user")
    public ResponseEntity<List<MessageResponse>> getConversation(
            @PathVariable String otherUserId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(messageService.getConversation(userDetails.getUsername(), otherUserId));
    }

    @GetMapping
    @Operation(summary = "Get all messages for current user")
    public ResponseEntity<List<MessageResponse>> getUserMessages(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(messageService.getUserMessages(userDetails.getUsername()));
    }

    @PutMapping("/read/{otherUserId}")
    @Operation(summary = "Mark conversation as read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable String otherUserId,
            @AuthenticationPrincipal UserDetails userDetails) {
        messageService.markAsReadConversation(userDetails.getUsername(), otherUserId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread message count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(messageService.getUnreadCount(userDetails.getUsername()));
    }
}
