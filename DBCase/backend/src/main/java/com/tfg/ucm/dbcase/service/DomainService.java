package com.tfg.ucm.dbcase.service;

import com.tfg.ucm.dbcase.dto.AddDomainRequest;
import com.tfg.ucm.dbcase.dto.CustomDomain;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.model.UserDomain;
import com.tfg.ucm.dbcase.repository.UserRepository;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DomainService {

    private final UserRepository userRepository;

    public Set<CustomDomain> getAll(Long id) {
        Set<CustomDomain> all = new LinkedHashSet<>();
        Arrays.stream(Domain.values()).map(d -> new CustomDomain(d.name(), null)).forEach(all::add);
        userRepository
                .findUserById(id)
                .ifPresent(
                        user ->
                                user.getCustomDomains().stream()
                                        .map(ud -> new CustomDomain(ud.getName(), ud.getBase()))
                                        .forEach(all::add));
        return all;
    }

    @Transactional
    public void addDomain(Long id, AddDomainRequest request) {
        userRepository
                .findUserById(id)
                .ifPresent(
                        user ->
                                user.getCustomDomains()
                                        .add(new UserDomain(request.getName(), request.getBase())));
    }

    @Transactional
    public void deleteDomain(Long id, String name) {
        userRepository
                .findUserById(id)
                .ifPresent(
                        user -> user.getCustomDomains().removeIf(ud -> ud.getName().equals(name)));
    }
}
