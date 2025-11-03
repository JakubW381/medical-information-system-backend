package com.example.his.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogPageRequest {

    private int page;
    private String search;

    private Long id;

    private String logType;

    private Long authorId;
    private Long targetId;

    private LocalDateTime from;
    private LocalDateTime to;
}
