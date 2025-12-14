package com.multiservice.ussd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UssdHistoryResponse {

    private Long id;
    private String phoneNumber;
    private String message;
    private String status;
    private LocalDateTime sentAt;
}
