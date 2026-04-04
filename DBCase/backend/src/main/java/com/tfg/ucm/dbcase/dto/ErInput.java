package com.tfg.ucm.dbcase.dto;

import java.util.List;

public record ErInput(
        List<ErEntityDTO> entities,
        List<ErRelationshipDTO> relationships,
        List<ErAttributeDTO> attributes,
        List<ErDomainDTO> domains)
        implements DiagramInput {}
