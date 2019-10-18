package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.Address;
import uk.gov.justice.digital.nomis.api.KeyValue;
import uk.gov.justice.digital.nomis.api.Phone;
import uk.gov.justice.digital.nomis.jpa.entity.AddressPhone;
import uk.gov.justice.digital.nomis.jpa.entity.AddressUsage;
import uk.gov.justice.digital.nomis.jpa.entity.ReferenceCodePK;
import uk.gov.justice.digital.nomis.jpa.repository.ReferenceCodesRepository;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;

@Component
public class AddressesTransformer {

    private static final String CITY = "CITY";
    private static final String COUNTY = "COUNTY";
    private static final String COUNTRY = "COUNTRY";
    private static final String ADDRESS_TYPE = "ADDRESS_TYPE";

    private static final Timestamp EARLIEST = new Timestamp(0);

    private static final Function<uk.gov.justice.digital.nomis.jpa.entity.AddressUsage, Timestamp> LAST_MODIFIED_ADDRESS_OF = (uk.gov.justice.digital.nomis.jpa.entity.AddressUsage address) ->
            Stream.of(
                    Optional.ofNullable(address.getModifyDatetime()).orElse(EARLIEST),
                    Optional.ofNullable(address.getCreateDatetime()).orElse(EARLIEST)
            )
                    .max(comparing(Timestamp::getTime))
                    .get();


    private static final Comparator<uk.gov.justice.digital.nomis.jpa.entity.AddressUsage> BY_ADDRESS_USAGE_MODIFIED = Comparator
            .comparing(LAST_MODIFIED_ADDRESS_OF)
            .reversed();

    private static final Function<uk.gov.justice.digital.nomis.jpa.entity.AddressPhone, Timestamp> LAST_MODIFIED_PHONE_OF = (uk.gov.justice.digital.nomis.jpa.entity.AddressPhone address) ->
            Stream.of(
                    Optional.ofNullable(address.getModifyDatetime()).orElse(EARLIEST),
                    Optional.ofNullable(address.getCreateDatetime()).orElse(EARLIEST)
            )
                    .max(comparing(Timestamp::getTime))
                    .get();


    private static final Comparator<uk.gov.justice.digital.nomis.jpa.entity.AddressPhone> BY_ADDRESS_PHONE_MODIFIED = Comparator
            .comparing(LAST_MODIFIED_PHONE_OF)
            .reversed();

    private final TypesTransformer typesTransformer;
    private final ReferenceCodesRepository referenceCodesRepository;

    @Autowired
    public AddressesTransformer(final TypesTransformer typesTransformer, final ReferenceCodesRepository referenceCodesRepository) {
        this.typesTransformer = typesTransformer;
        this.referenceCodesRepository = referenceCodesRepository;
    }

    public Address addressOf(final uk.gov.justice.digital.nomis.jpa.entity.Address address) {
        return Address.builder()
                .addressId(address.getAddressId())
                .addressType(address.getAddressType())
                .addressUsages(addressUsagesOf(address.getAddressUsages()))
                .businessHour(address.getBusinessHour())
                .capacity(address.getCapacity())
                .cityCode(address.getCityCode())
                .city(cityOf(address))
                .cityName(address.getCityName())
                .comments(address.getCommentText())
                .contactPersonName(address.getContactPersonName())
                .countyCode(address.getCountyCode())
                .county(countyOf(address))
                .countryCode(address.getCountryCode())
                .country(countryOf(address))
                .flat(address.getFlat())
                .relationship(address.getRelationship())
                .locality(address.getLocality())
                .mail(typesTransformer.ynToBoolean(address.getMailFlag()))
                .noFixedAddress(typesTransformer.ynToBoolean(address.getNoFixedAddressFlag()))
                .ownerClass(address.getOwnerClass())
                .ownerSeq(address.getOwnerSeq())
                .postalCode(address.getPostalCode())
                .premise(address.getPremise())
                .primary(typesTransformer.ynToBoolean(address.getPrimaryFlag()))
                .services(typesTransformer.ynToBoolean(address.getServicesFlag()))
                .specialNeedsCode(address.getSpecialNeedsCode())
                .street(address.getStreet())
                .type(address.getType())
                .validatedPaf(typesTransformer.ynToBoolean(address.getValidatedPafFlag()))
                .startDate(typesTransformer.localDateOf(address.getStartDate()))
                .endDate(typesTransformer.localDateOf(address.getEndDate()))
                .phones(phonesOf(address.getPhones()))
                .build();
    }

    private List<uk.gov.justice.digital.nomis.api.AddressUsage> addressUsagesOf(final List<AddressUsage> addressUsages) {
        return Optional.ofNullable(addressUsages).map(
                usages -> usages.stream()
                        .sorted(BY_ADDRESS_USAGE_MODIFIED)
                        .map(usage -> uk.gov.justice.digital.nomis.api.AddressUsage.builder()
                                .active(typesTransformer.ynToBoolean(usage.getActiveFlag()))
                                .usage(addressUsageOf(usage))
                                .build())
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    private List<Phone> phonesOf(final List<AddressPhone> phones) {
        return phones.stream()
                .sorted(BY_ADDRESS_PHONE_MODIFIED)
                .map(phone -> Phone.builder()
                        .extNo(phone.getExtNo())
                        .ownerClass(phone.getOwnerClass())
                        .ownerCode(phone.getOwnerCode())
                        .ownerSeq(phone.getOwnerSeq())
                        .phoneId(phone.getPhoneId())
                        .phoneNo(phone.getPhoneNo())
                        .phoneType(phone.getPhoneType())
                        .relationship(phone.getRelationship())
                        .build())
                .collect(Collectors.toList());
    }

    private KeyValue addressUsageOf(final AddressUsage au) {
        return referenceOf(au.getAddressUsage(), ADDRESS_TYPE);
    }

    private KeyValue cityOf(final uk.gov.justice.digital.nomis.jpa.entity.Address address) {
        return referenceOf(address.getCityCode(), CITY);
    }

    private KeyValue countyOf(final uk.gov.justice.digital.nomis.jpa.entity.Address address) {
        return referenceOf(address.getCountyCode(), COUNTY);
    }

    private KeyValue countryOf(final uk.gov.justice.digital.nomis.jpa.entity.Address address) {
        return referenceOf(address.getCountryCode(), COUNTRY);
    }

    private KeyValue referenceOf(final String code, final String domain) {
        return referenceCodesRepository.findById(ReferenceCodePK.builder().code(code).domain(domain).build())
                .map(rc -> KeyValue.builder().code(rc.getCode()).description(rc.getDescription()).build())
                .orElse(null);
    }
}
