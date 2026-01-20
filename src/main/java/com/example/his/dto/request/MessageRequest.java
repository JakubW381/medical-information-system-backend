package com.example.his.dto.request;

import lombok.Data;

@Data
public class MessageRequest {
    private String email;
    private String subject;
    private String content;
}
