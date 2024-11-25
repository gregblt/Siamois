package fr.siamois.services;

import fr.siamois.models.StratigraphicRelationship;
import fr.siamois.repositories.StratigraphicRelationshipRepository;
import org.springframework.stereotype.Service;

@Service
public class StratigraphicRelationshipService {

    private final StratigraphicRelationshipRepository stratigraphicRelationshipRepository;

    public StratigraphicRelationshipService(StratigraphicRelationshipRepository stratigraphicRelationshipRepository) {
        this.stratigraphicRelationshipRepository = stratigraphicRelationshipRepository;
    }

    /**
     * Insert a stratigraphic relationship
     *
     * @param stratigraphicRelationship - The relationship to save
     * @return The relationship
     * @throws RuntimeException If the repository method throws an Exception
     */
    public StratigraphicRelationship save(StratigraphicRelationship stratigraphicRelationship)   {
        return this.stratigraphicRelationshipRepository.save(stratigraphicRelationship);
    }
}
