package uk.gov.justice.digital.nomis.jpa.entity;

import lombok.Builder;
import lombok.Value;

import java.sql.Timestamp;

@Builder
@Value
public class LabelledTimestamp implements Comparable<LabelledTimestamp>{
    private final String label;
    private final Timestamp timestamp;

    @Override
    public int compareTo(final LabelledTimestamp o) {
        return timestamp.compareTo(o.timestamp);
    }
}
