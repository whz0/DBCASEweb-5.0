package com.tfg.ucm.dbcase.dto.input;

import com.tfg.ucm.dbcase.dto.erdiagram.ErAttributeDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErDomainDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErEntityDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErRelationshipDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErUndefinedDTO;
import java.util.List;

public record ErInput(
        List<ErEntityDTO> entities,
        List<ErRelationshipDTO> relationships,
        List<ErAttributeDTO> attributes,
        List<ErDomainDTO> domains,
        List<ErUndefinedDTO> undefineds)
        implements DiagramInput {}
