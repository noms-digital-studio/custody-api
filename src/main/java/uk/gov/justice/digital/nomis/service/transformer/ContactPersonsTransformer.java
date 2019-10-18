package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.ContactPersonType;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderContactPerson;

import java.util.Optional;

@Component
public class ContactPersonsTransformer {

    private final TypesTransformer typesTransformer;
    private final PersonTransformer personTransformer;


    @Autowired
    public ContactPersonsTransformer(final TypesTransformer typesTransformer, final PersonTransformer personTransformer) {
        this.typesTransformer = typesTransformer;
        this.personTransformer = personTransformer;
    }

    public uk.gov.justice.digital.nomis.api.OffenderContactPerson offenderContactPersonOf(final OffenderContactPerson offenderContactPerson) {
        return Optional.ofNullable(offenderContactPerson)
                .map(ocp -> uk.gov.justice.digital.nomis.api.OffenderContactPerson.builder()
                        .active(typesTransformer.ynToBoolean(ocp.getActiveFlag()))
                        .addresses(personTransformer.addressesOf(ocp.getPerson()))
                        .approvedVisitor(typesTransformer.ynToBoolean(ocp.getApprovedVisitorFlag()))
                        .awareOfCharges(typesTransformer.ynToBoolean(ocp.getAwareOfChargesFlag()))
                        .bookingId(ocp.getOffenderBookId())
                        .canBeContacted(typesTransformer.ynToBoolean(ocp.getCanBeContactedFlag()))
                        .caseInfoNumber(ocp.getCaseInfoNumber())
                        .caseloadType(ocp.getCaseloadType())
                        .comments(ocp.getCommentText())
                        .contactRootOffenderId(ocp.getContactRootOffenderId())
                        .emergencyContact(typesTransformer.ynToBoolean(ocp.getEmergencyContactFlag()))
                        .nextOfKin(typesTransformer.ynToBoolean(ocp.getNextOfKinFlag()))
                        .offenderContactPersonId(ocp.getOffenderContactPersonId())
                        .person(personTransformer.personOf(ocp.getPerson()))
                        .contactPersonType(contactPersonTypeOf(ocp.getContactPersonType()))
                        .build())
                .orElse(null);
    }

    private ContactPersonType contactPersonTypeOf(final uk.gov.justice.digital.nomis.jpa.entity.ContactPersonType contactPersonType) {
        return Optional.ofNullable(contactPersonType)
                .map(cpt -> ContactPersonType.builder()
                        .contactType(cpt.getContactType())
                        .relationshipType(cpt.getRelationshipType())
                        .active(typesTransformer.ynToBoolean(cpt.getActiveFlag()))
                        .contactClass(cpt.getContactClass())
                        .expiryDate(typesTransformer.localDateOf(cpt.getExpiryDate()))
                        .listSeq(cpt.getListSeq())
                        .updateAllowed(typesTransformer.ynToBoolean(cpt.getUpdateAllowedFlag()))
                        .build())
                .orElse(null);
    }
}
