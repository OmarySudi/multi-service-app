package com.multiservice.tips.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipRequest {

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Content is required")
    private String content;
}
