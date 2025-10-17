package com.example.his.service.search.implementation;

import com.example.his.dto.request.DocumentPageRequest;
import com.example.his.dto.response.PageResponse;
import com.example.his.dto.request.PatientsPageRequest;
import com.example.his.model.Document;
import com.example.his.model.user.User;
import com.example.his.repository.DocumentRepository;
import com.example.his.repository.UserRepository;
import com.example.his.repository.jpa_specification.DocumentSpecification;
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

    public CriteriaBuilderSearch(UserRepository userRepository, DocumentRepository documentRepository) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    public PageResponse<User> userPaginationSearch(PatientsPageRequest pageDto) {
        Specification<User> spec = UserSpecification.filterUser(pageDto);

        int page = pageDto.getPage() > 0 ? pageDto.getPage() - 1 : 0;
        int size = 20;
        Pageable pageable = PageRequest.of(page, size);

        Page springPage = userRepository.findAll(spec, pageable);

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
        int size = 20;
        Pageable pageable = PageRequest.of(page, size);

        Page springPage = documentRepository.findAll(spec, pageable);

        PageResponse<Document> dtoPage = new PageResponse<>();
        dtoPage.setItems(springPage.getContent());
        dtoPage.setSize(springPage.getSize());
        dtoPage.setCurrent(springPage.getNumber());
        dtoPage.setTotalElements(springPage.getTotalElements());
        dtoPage.setTotalPages(springPage.getTotalPages());

        return dtoPage;
    }
}
