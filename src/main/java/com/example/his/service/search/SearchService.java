package com.example.his.service.search;

import com.example.his.dto.PatientProfileDto;
import com.example.his.dto.request.DocumentPageRequest;
import com.example.his.dto.request.LogPageRequest;
import com.example.his.dto.request.UserPageRequest;
import com.example.his.dto.response.PageResponse;
import com.example.his.dto.request.PatientsPageRequest;
import com.example.his.model.Document;
import com.example.his.model.logs.Log;
import com.example.his.model.user.User;

public interface SearchService {
    PageResponse<PatientProfileDto> patientPaginationSearch(PatientsPageRequest pageDto);
    PageResponse<User> userPaginationSearch(UserPageRequest pageDto);
    PageResponse<Document> documentPaginationSearch(DocumentPageRequest pageDto, User patient);
    PageResponse<Log> logPagination(LogPageRequest pageDto);
}
