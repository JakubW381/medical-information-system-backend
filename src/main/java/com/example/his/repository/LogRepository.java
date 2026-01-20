package com.example.his.repository;

import com.example.his.model.logs.Log;
import com.example.his.model.logs.LogType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, Long>, JpaSpecificationExecutor<Log> {
    List<Log> findByLogType(LogType logType);
}
