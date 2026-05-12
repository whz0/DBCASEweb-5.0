package com.tfg.ucm.dbcase.controller;

import com.tfg.ucm.dbcase.dto.AddDomainRequest;
import com.tfg.ucm.dbcase.dto.CustomDomain;
import com.tfg.ucm.dbcase.service.DomainService;
import com.tfg.ucm.dbcase.service.UserService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/domain")
@RequiredArgsConstructor
public class DomainController {

    private final DomainService domainService;
    private final UserService userService;

    @GetMapping("/data-types")
    public ResponseEntity<Set<CustomDomain>> getAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getCurrentUser(userDetails.getUsername()).getId();
        return ResponseEntity.ok(domainService.getAll(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<String> addDomain(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AddDomainRequest request) {
        Long userId = userService.getCurrentUser(userDetails.getUsername()).getId();
        domainService.addDomain(userId, request);
        return ResponseEntity.ok("Dominio guardado con éxito");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteDomain(
            @AuthenticationPrincipal UserDetails userDetails, @RequestBody String name) {
        Long userId = userService.getCurrentUser(userDetails.getUsername()).getId();
        domainService.deleteDomain(userId, name);
        return ResponseEntity.ok("Se ha borrado el dominio con éxito");
    }
}
