package com.example.his.service.search;

import com.example.his.dto.request.DocumentPageRequest;
import com.example.his.dto.response.PageResponse;
import com.example.his.dto.request.PatientsPageRequest;
import com.example.his.model.Document;
import com.example.his.model.user.User;

public interface SearchService {
    PageResponse<User> userPaginationSearch(PatientsPageRequest pageDto);
    PageResponse<Document> documentPaginationSearch(DocumentPageRequest pageDto, User patient);
}
