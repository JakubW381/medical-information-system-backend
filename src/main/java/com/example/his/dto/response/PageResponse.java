package com.example.his.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {

    private List<T> items;
    private int size;
    private int current;
    private long totalElements;
    private int totalPages;

}
