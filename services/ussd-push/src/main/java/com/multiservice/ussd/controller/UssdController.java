package com.multiservice.ussd.controller;

import com.multiservice.ussd.dto.UssdPushRequest;
import com.multiservice.ussd.dto.UssdPushResponse;
import com.multiservice.ussd.dto.UssdHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.multiservice.common.constants.Permissions.*;

@RestController
@RequestMapping("/api/ussd")
@RequiredArgsConstructor
public class UssdController {

    @PostMapping("/send")
    @PreAuthorize("hasAuthority('" + USSD_SEND + "')")
    public ResponseEntity<UssdPushResponse> sendUssdPush(
            @RequestBody UssdPushRequest request,
            Authentication auth) {
        // Mock response
        UssdPushResponse response = UssdPushResponse.builder()
                .id(1L)
                .phoneNumber(request.getPhoneNumber())
                .message(request.getMessage())
                .status("sent")
                .sentAt(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('" + USSD_HISTORY + "')")
    public ResponseEntity<List<UssdHistoryResponse>> getHistory() {
        // Mock response
        List<UssdHistoryResponse> history = new ArrayList<>();
        history.add(UssdHistoryResponse.builder()
                .id(1L)
                .phoneNumber("+255712345678")
                .message("Account balance inquiry")
                .status("delivered")
                .sentAt(LocalDateTime.now().minusHours(2))
                .build());
        history.add(UssdHistoryResponse.builder()
                .id(2L)
                .phoneNumber("+255723456789")
                .message("Transaction notification")
                .status("delivered")
                .sentAt(LocalDateTime.now().minusHours(1))
                .build());
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + USSD_VIEW + "')")
    public ResponseEntity<UssdPushResponse> getUssdPush(@PathVariable Long id) {
        // Mock response
        UssdPushResponse response = UssdPushResponse.builder()
                .id(id)
                .phoneNumber("+255712345678")
                .message("Test message")
                .status("delivered")
                .sentAt(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/config")
    @PreAuthorize("hasAuthority('" + USSD_CONFIG + "')")
    public ResponseEntity<String> updateConfig(@RequestBody String config) {
        return ResponseEntity.ok("USSD configuration updated successfully");
    }

    @GetMapping("/public/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("USSD Push Service is running!");
    }
}
