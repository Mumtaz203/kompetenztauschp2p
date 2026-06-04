package de.thws.kompetenz.matching_request.adapter.out.persistence.mapper;

import de.thws.kompetenz.matching_request.adapter.out.persistence.entity.MatchRequestEntity;
import de.thws.kompetenz.matching_request.domain.MatchRequestModel;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MatchRequestPersistenceMapper {

    public MatchRequestModel toModel(MatchRequestEntity entity) {
        if (entity == null) return null;
        MatchRequestModel model = new MatchRequestModel();
        model.setId(entity.getId());
        model.setSenderId(entity.getSenderId());
        model.setReceiverId(entity.getReceiverId());
        model.setStatus(entity.getStatus());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());
        return model;

    }

    public MatchRequestEntity toEntity(MatchRequestModel model){
        if (model == null) return null;
        MatchRequestEntity entity = new MatchRequestEntity();//CreatedAt,UpdatedAt are @with prepersist and @with preupdate

        entity.setSenderId(model.getSenderId());
        entity.setReceiverId(model.getReceiverId());
        entity.setStatus(model.getStatus());
        return entity;

    }

    public void updateEntityFromDomain(MatchRequestModel model, MatchRequestEntity entity) {
        if (model == null || entity == null) {
            return;
        }

        entity.setSenderId(model.getSenderId());
        entity.setReceiverId(model.getReceiverId());
        entity.setStatus(model.getStatus());
    }
}
