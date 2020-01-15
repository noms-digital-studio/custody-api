package uk.gov.justice.digital.nomis.jpa.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

@Data
public class OffenderPhysicalAttributesPK implements Serializable {
    @Column(name = "OFFENDER_BOOK_ID")
    @Id
    private Long offenderBookId;
    @Column(name = "ATTRIBUTE_SEQ")
    @Id
    private Long attributeSeq;

}
