package de.thws.kompetenz.matching_request.adapter.in.rest.mapper;

import de.thws.kompetenz.matching_request.adapter.in.rest.dto.MatchRequestResponseDTO;

import de.thws.kompetenz.matching_request.domain.MatchRequestModel;
import de.thws.kompetenz.matching_request.domain.MatchRequestStatus;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MatchRequestRestMapper {



    public MatchRequestResponseDTO toResponseDTO(MatchRequestModel model) {
        if (model == null) {
            return null;
        }

        return new MatchRequestResponseDTO(
                model.getId(),
                model.getSenderId(),
                model.getReceiverId(),
                model.getStatus(),
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
    }

    public List<MatchRequestResponseDTO> toResponseDTOList(List<MatchRequestModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}