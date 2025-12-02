package com.example.his.service.search.implementation;

import com.example.his.dto.PatientProfileDto;
import com.example.his.dto.request.DocumentPageRequest;
import com.example.his.dto.request.LogPageRequest;
import com.example.his.dto.request.UserPageRequest;
import com.example.his.dto.response.PageResponse;
import com.example.his.dto.request.PatientsPageRequest;
import com.example.his.model.Document;
import com.example.his.model.logs.Log;
import com.example.his.model.user.PatientProfile;
import com.example.his.model.user.User;
import com.example.his.repository.DocumentRepository;
import com.example.his.repository.LogRepository;
import com.example.his.repository.UserRepository;
import com.example.his.repository.jpa_specification.DocumentSpecification;
import com.example.his.repository.jpa_specification.LogSpecification;
import com.example.his.repository.jpa_specification.PatientSpecification;
import com.example.his.repository.jpa_specification.UserSpecification;
import com.example.his.service.search.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class CriteriaBuilderSearch implements SearchService {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final LogRepository logRepository;

    public CriteriaBuilderSearch(UserRepository userRepository, DocumentRepository documentRepository, LogRepository logRepository) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.logRepository = logRepository;
    }

    @Override
    public PageResponse<PatientProfileDto> patientPaginationSearch(PatientsPageRequest pageDto) {
        var spec = PatientSpecification.filterUser(pageDto);

        int page = pageDto.getPage() > 0 ? pageDto.getPage() - 1 : 0;
        int size = 6;
        Pageable pageable = PageRequest.of(page, size);

        Page<User> springPage = userRepository.findAll(spec, pageable);

        // mapowanie User -> PatientProfileDto
        Page<PatientProfileDto> dtoPage = springPage.map(user -> {
            if (user.getPatientProfile() != null) {
                return user.getPatientProfile().toDto();
            }
            return null;
        });

        PageResponse<PatientProfileDto> response = new PageResponse<>();
        response.setItems(dtoPage.getContent());
        response.setSize(dtoPage.getSize());
        response.setCurrent(dtoPage.getNumber());
        response.setTotalElements(dtoPage.getTotalElements());
        response.setTotalPages(dtoPage.getTotalPages());

        return response;
    }

    @Override
    public PageResponse<User> userPaginationSearch(UserPageRequest pageDto) {
        Specification<User> spec = UserSpecification.filterUsers(pageDto);

        int page = pageDto.getPage() > 0 ? pageDto.getPage() - 1 : 0;
        int size = 12;
        Pageable pageable = PageRequest.of(page, size);

        Page<User> springPage = userRepository.findAll(spec, pageable);

        PageResponse<User> dtoPage = new PageResponse<>();
        dtoPage.setItems(springPage.getContent());
        dtoPage.setSize(springPage.getSize());
        dtoPage.setCurrent(springPage.getNumber());
        dtoPage.setTotalElements(springPage.getTotalElements());
        dtoPage.setTotalPages(springPage.getTotalPages());

        return dtoPage;
    }

    @Override
    public PageResponse<Document> documentPaginationSearch(DocumentPageRequest pageDto, User patient) {
        Specification<Document> spec = DocumentSpecification.filterDocuments(pageDto, patient);

        int page = pageDto.getPage() > 0 ? pageDto.getPage() - 1 : 0;
        int size = 12;
        Pageable pageable = PageRequest.of(page, size);

        Page<Document> springPage = documentRepository.findAll(spec, pageable);

        PageResponse<Document> dtoPage = new PageResponse<>();
        dtoPage.setItems(springPage.getContent());
        dtoPage.setSize(springPage.getSize());
        dtoPage.setCurrent(springPage.getNumber());
        dtoPage.setTotalElements(springPage.getTotalElements());
        dtoPage.setTotalPages(springPage.getTotalPages());

        return dtoPage;
    }

    @Override
    public PageResponse<Log> logPagination(LogPageRequest pageDto) {
        Specification<Log> spec = LogSpecification.filterLogs(pageDto);

        int page = pageDto.getPage() > 0 ? pageDto.getPage() - 1 : 0;
        int size = 30;
        Pageable pageable = PageRequest.of(page, size);

        Page<Log> springPage = logRepository.findAll(spec, pageable);

        PageResponse<Log> dtoPage = new PageResponse<>();
        dtoPage.setItems(springPage.getContent());
        dtoPage.setSize(springPage.getSize());
        dtoPage.setCurrent(springPage.getNumber());
        dtoPage.setTotalElements(springPage.getTotalElements());
        dtoPage.setTotalPages(springPage.getTotalPages());

        return dtoPage;
    }
}
