package com.multiservice.tips.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipResponse {

    private Long id;
    private String category;
    private String content;
    private String status; // approved, pending, rejected
}
