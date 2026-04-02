package com.tfg.ucm.dbcase.controller;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.TransformRequest;
import com.tfg.ucm.dbcase.service.DiagramContext;
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

    private final DiagramContext diagramContext;

    @PostMapping("/generate")
    public ResponseEntity<Object> generate(@RequestBody TransformRequest request) {

        Object o = null;
        try {
            Diagram d = diagramContext.generate(request.getType(), request.getDiagram());
            o = diagramContext.transform(request.getTransformTo(), d);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(o);
    }
}
