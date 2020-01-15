package uk.gov.justice.digital.nomis.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileCodePK implements Serializable {
    @Id
    @Column(name = "PROFILE_TYPE")
    private String profileType;
    @Id
    @Column(name = "PROFILE_CODE")
    private String profileCode;
}
