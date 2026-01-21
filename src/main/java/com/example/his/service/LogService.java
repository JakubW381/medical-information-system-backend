package com.example.his.service;

import com.example.his.dto.request.LogPageRequest;
import com.example.his.dto.response.LogRecordDto;
import com.example.his.dto.response.PageResponse;
import com.example.his.model.logs.Log;
import com.example.his.repository.LogRepository;
import com.example.his.service.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LogService {

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private SearchService searchService;

    @CacheEvict(value = "logPageCache", allEntries = true)
    public void saveLog(Log log) {
        logRepository.save(log);
    }

    @Cacheable("logPageCache")
    public PageResponse<LogRecordDto> getLogPage(LogPageRequest request) {
        PageResponse<Log> logPage = searchService.logPagination(request);

        List<LogRecordDto> recordDtos = new ArrayList<>();

        for (Log log : logPage.getItems()) {
            LogRecordDto recordDto = new LogRecordDto(
                    log.getId(),
                    log.getAuthor() != null ? log.getAuthor().toSafeUserDto() : null,
                    log.getLogType(),
                    log.getTarget() != null ? log.getTarget().toSafeUserDto() : null,
                    log.getDescription(),
                    log.getTimestamp());
            recordDtos.add(recordDto);
        }

        PageResponse<LogRecordDto> logDtoPages = new PageResponse<>();

        logDtoPages.setItems(recordDtos);
        logDtoPages.setTotalPages(logPage.getTotalPages());
        logDtoPages.setSize(logPage.getSize());
        logDtoPages.setTotalElements(logPage.getTotalElements());
        logDtoPages.setCurrent(logPage.getCurrent());

        return logDtoPages;
    }
}
