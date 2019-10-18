package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.jpa.entity.AgencyInternalLocation;
import uk.gov.justice.digital.nomis.jpa.entity.AgencyLocation;

import java.util.Optional;

@Component
public class ReferenceDataTransformer {

    private final TypesTransformer typesTransformer;

    public ReferenceDataTransformer(final TypesTransformer typesTransformer) {
        this.typesTransformer = typesTransformer;
    }


    public uk.gov.justice.digital.nomis.api.AgencyLocation agencyLocationOf(final AgencyLocation agencyLocation) {
        return Optional.ofNullable(agencyLocation).map(
                al -> uk.gov.justice.digital.nomis.api.AgencyLocation.builder()
                        .abbreviation(al.getAbbreviation())
                        .active(typesTransformer.ynToBoolean(al.getActiveFlag()))
                        .addressType(al.getAddressType())
                        .agencyLocationId(al.getAgyLocId())
                        .agencyLocationType(al.getAgencyLocationType())
                        .areaCode(al.getAreaCode())
                        .bailOffice(typesTransformer.ynToBoolean(al.getBailOfficeFlag()))
                        .businessHours(al.getBusinessHours())
                        .cjitCode(al.getCjitCode())
                        .commissaryPrivilege(al.getCommissaryPrivilege())
                        .contactName(al.getContactName())
                        .deactivationDate(typesTransformer.localDateOf(al.getDeactivationDate()))
                        .description(al.getDescription())
                        .disabilityAccessCode(al.getDisabilityAccessCode())
                        .districtCode(al.getDistrictCode())
                        .geographicRegionCode(al.getGeographicRegionCode())
                        .housingLev1Code(al.getHousingLev1Code())
                        .housingLev2Code(al.getHousingLev2Code())
                        .housingLev3Code(al.getHousingLev3Code())
                        .housingLev4Code(al.getHousingLev4Code())
                        .jurisdictionCode(al.getJurisdictionCode())
                        .justiceAreaCode(al.getJusticeAreaCode())
                        .listSeq(al.getListSeq())
                        .longDescription(al.getLongDescription())
                        .nomsRegionCode(al.getNomsRegionCode())
                        .propertyLev1Code(al.getPropertyLev1Code())
                        .propertyLev2Code(al.getPropertyLev2Code())
                        .propertyLev3Code(al.getPropertyLev3Code())
                        .serviceRequired(typesTransformer.ynToBoolean(al.getServiceRequiredFlag()))
                        .subAreaCode(al.getSubAreaCode())
                        .build())
                .orElse(null);
    }

    public uk.gov.justice.digital.nomis.api.AgencyInternalLocation agencyInternalLocationOf(final AgencyInternalLocation agencyInternalLocation) {
        return Optional.ofNullable(agencyInternalLocation).map(
                ail -> uk.gov.justice.digital.nomis.api.AgencyInternalLocation.builder()
                        .acaCapRating(ail.getAcaCapRating())
                        .active(typesTransformer.ynToBoolean(ail.getActiveFlag()))
                        .agencyLocationId(ail.getAgyLocId())
                        .capacity(ail.getCapacity())
                        .certified(typesTransformer.ynToBoolean(ail.getCertifiedFlag()))
                        .cnaNumber(ail.getCnaNo())
                        .comments(ail.getCommentText())
                        .deactivateDate(typesTransformer.localDateOf(ail.getDeactivateDate()))
                        .deactivateReasonCode(ail.getDeactivateReasonCode())
                        .description(ail.getDescription())
                        .internalLocationCode(ail.getInternalLocationCode())
                        .internalLocationId(ail.getInternalLocationId())
                        .internalLocationType(ail.getInternalLocationType())
                        .listSequence(ail.getListSeq())
                        .numberOfOccupants(ail.getNoOfOccupant())
                        .operationCapacity(ail.getOperationCapacity())
                        .parentInternalLocationId(ail.getParentInternalLocationId())
                        .reactivateDate(typesTransformer.localDateOf(ail.getReactivateDate()))
                        .securityLevelCode(ail.getSecurityLevelCode())
                        .trackingFlag(ail.getTrackingFlag())
                        .unitType(ail.getUnitType())
                        .userDesc(ail.getUserDesc())
                        .build()).orElse(null);
    }
}
