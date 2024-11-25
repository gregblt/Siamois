package fr.siamois.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class StratigraphicRelationshipKey implements Serializable {

    private RecordingUnit fk_recording_unit_1_id;
    private RecordingUnit fk_recording_unit_2_id;

}
