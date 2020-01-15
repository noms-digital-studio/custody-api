package uk.gov.justice.digital.nomis.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.nomis.jpa.entity.ProfileCode;
import uk.gov.justice.digital.nomis.jpa.entity.ProfileCodePK;

@Repository
public interface ProfileCodesRepository extends JpaRepository<ProfileCode, ProfileCodePK> {
}
