package fr.siamois.repositories;

import fr.siamois.models.StratigraphicRelationship;
import fr.siamois.models.StratigraphicRelationshipKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StratigraphicRelationshipRepository extends CrudRepository<StratigraphicRelationship, StratigraphicRelationshipKey> {

}

