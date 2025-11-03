package com.example.his.dto.response;

import com.example.his.dto.SafeUserDto;
import com.example.his.model.logs.LogType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogRecordDto {
    private Long id;
    private SafeUserDto author;
    private LogType logType;
    private SafeUserDto target;
    private String description;
    private LocalDateTime timestamp;
}
