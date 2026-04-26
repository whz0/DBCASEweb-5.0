package com.tfg.ucm.dbcase.controller;

import com.tfg.ucm.dbcase.dto.ExecuteSqlRequest;
import com.tfg.ucm.dbcase.dto.TestDatabaseRequest;
import com.tfg.ucm.dbcase.service.DatabaseExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/database")
@RequiredArgsConstructor
public class DatabaseController {

    private final DatabaseExecutionService databaseExecutionService;

    @PostMapping("/test")
    public ResponseEntity<String> test(@RequestBody TestDatabaseRequest request) {
        if (databaseExecutionService.test(request)) {
            return ResponseEntity.ok("Se ha realizado la conexión con la base de datos");
        } else {
            return ResponseEntity.badRequest().body("Ha fallado la conexión");
        }
    }

    @PostMapping("/execute")
    public ResponseEntity<String> execute(@RequestBody ExecuteSqlRequest request) {
        try {
            databaseExecutionService.execute(request);
            return ResponseEntity.ok("SQL ejecutado con éxito");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
