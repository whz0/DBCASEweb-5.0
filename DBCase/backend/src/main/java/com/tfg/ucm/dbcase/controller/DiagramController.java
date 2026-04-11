package com.tfg.ucm.dbcase.controller;

import com.tfg.ucm.dbcase.dto.TransformRequest;
import com.tfg.ucm.dbcase.service.DiagramTransformationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diagram")
@RequiredArgsConstructor
public class DiagramController {

    private final DiagramTransformationService diagramTransformationService;

    @PostMapping("/generate")
    public ResponseEntity<Object> generate(@RequestBody TransformRequest request) {
        try {
            return ResponseEntity.ok(diagramTransformationService.transformDiagram(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
