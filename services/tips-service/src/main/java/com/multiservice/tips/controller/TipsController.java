package com.multiservice.tips.controller;

import com.multiservice.tips.dto.TipRequest;
import com.multiservice.tips.dto.TipResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.multiservice.common.constants.Permissions.*;

@RestController
@RequestMapping("/api/tips")
@RequiredArgsConstructor
public class TipsController {

    @GetMapping
    @PreAuthorize("hasAuthority('" + TIPS_READ + "')")
    public ResponseEntity<List<TipResponse>> getAllTips() {
        // Mock response
        List<TipResponse> tips = new ArrayList<>();
        tips.add(new TipResponse(1L, "Productivity Tip", "Use keyboard shortcuts", "approved"));
        tips.add(new TipResponse(2L, "Health Tip", "Drink 8 glasses of water daily", "pending"));
        return ResponseEntity.ok(tips);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + TIPS_CREATE + "')")
    public ResponseEntity<TipResponse> createTip(@RequestBody TipRequest request, Authentication auth) {
        // Mock response
        TipResponse response = new TipResponse(3L, request.getCategory(), request.getContent(), "pending");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + TIPS_UPDATE + "')")
    public ResponseEntity<TipResponse> updateTip(
            @PathVariable Long id,
            @RequestBody TipRequest request) {
        // Mock response
        TipResponse response = new TipResponse(id, request.getCategory(), request.getContent(), "pending");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('" + TIPS_APPROVE + "')")
    public ResponseEntity<TipResponse> approveTip(@PathVariable Long id) {
        // Mock response
        TipResponse response = new TipResponse(id, "Category", "Content", "approved");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + TIPS_DELETE + "')")
    public ResponseEntity<Void> deleteTip(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/report")
    @PreAuthorize("hasAuthority('" + TIPS_REPORT + "')")
    public ResponseEntity<String> getTipsReport() {
        return ResponseEntity.ok("Tips Report: Total=100, Approved=85, Pending=15");
    }

    @GetMapping("/public/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Tips Service is running!");
    }
}
