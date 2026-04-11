package com.tfg.ucm.dbcase.dto;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true)
public class Relationship extends Node {
    private List<Participant> participants;
    private List<Attribute> attributes;

    public boolean isNM() {
        return participants != null
                && participants.stream()
                .filter(
                        p ->
                                p.getCardinality() != null
                                        && !p.getCardinality().equals("1"))
                .count()
                >= 2;
    }
}
