package fr.siamois.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Table(name = "stratigraphic_relationship")
public class StratigraphicRelationship {

//    @EmbeddedId
//    private StratigraphicRelationshipKey id;

    @Column(name = "fk_recording_unit_1_id")
    private RecordingUnit recording_unit_1;

    @Column(name = "fk_recording_unit_2_id")
    private RecordingUnit recording_unit_2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_relationship_concept_id")
    private Concept relationship;

}