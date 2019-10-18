package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.Address;
import uk.gov.justice.digital.nomis.api.Person;
import uk.gov.justice.digital.nomis.jpa.entity.AddressPhone;
import uk.gov.justice.digital.nomis.jpa.entity.AddressUsage;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;

@Component
public class PersonTransformer {

    private static final Timestamp EARLIEST = new Timestamp(0);

    private static final Function<List<AddressUsage>, Timestamp> LATEST_CREATED_USAGE_OF = (List<AddressUsage> usages) ->
            usages.stream().
                    map(x -> Optional.ofNullable(x.getCreateDatetime()).orElse(EARLIEST))
                    .max(comparing(Timestamp::getTime))
                    .orElse(EARLIEST);

    private static final Function<List<AddressUsage>, Timestamp> LATEST_MODIFIED_USAGE_OF = (List<AddressUsage> usages) ->
            usages.stream().
                    map(x -> Optional.ofNullable(x.getModifyDatetime()).orElse(EARLIEST))
                    .max(comparing(Timestamp::getTime))
                    .orElse(EARLIEST);

    private static final Function<List<AddressPhone>, Timestamp> LATEST_CREATED_PHONE_OF = (List<AddressPhone> usages) ->
            usages.stream().
                    map(x -> Optional.ofNullable(x.getCreateDatetime()).orElse(EARLIEST))
                    .max(comparing(Timestamp::getTime))
                    .orElse(EARLIEST);

    private static final Function<List<AddressPhone>, Timestamp> LATEST_MODIFIED_PHONE_OF = (List<AddressPhone> usages) ->
            usages.stream().
                    map(x -> Optional.ofNullable(x.getModifyDatetime()).orElse(EARLIEST))
                    .max(comparing(Timestamp::getTime))
                    .orElse(EARLIEST);

    private static final Function<uk.gov.justice.digital.nomis.jpa.entity.Address, Timestamp> LAST_MODIFIED_ADDRESS_OF = (uk.gov.justice.digital.nomis.jpa.entity.Address address) ->
            Stream.of(
                    Optional.ofNullable(address.getModifyDatetime()).orElse(EARLIEST),
                    Optional.ofNullable(address.getCreateDatetime()).orElse(EARLIEST),
                    LATEST_MODIFIED_USAGE_OF.apply(address.getAddressUsages()),
                    LATEST_CREATED_USAGE_OF.apply(address.getAddressUsages()),
                    LATEST_MODIFIED_PHONE_OF.apply(address.getPhones()),
                    LATEST_CREATED_PHONE_OF.apply(address.getPhones())
            )
                    .max(comparing(Timestamp::getTime))
                    .get();

    private static final Comparator<uk.gov.justice.digital.nomis.jpa.entity.Address> BY_ADDRESS_MODIFIED =
            Comparator.comparing(uk.gov.justice.digital.nomis.jpa.entity.Address::getPrimaryFlag, Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(LAST_MODIFIED_ADDRESS_OF)
                    .reversed();

    private final AddressesTransformer addressesTransformer;
    private final TypesTransformer typesTransformer;

    @Autowired
    public PersonTransformer(final AddressesTransformer addressesTransformer, final TypesTransformer typesTransformer) {
        this.addressesTransformer = addressesTransformer;
        this.typesTransformer = typesTransformer;
    }

    Person personOf(final uk.gov.justice.digital.nomis.jpa.entity.Person person) {
        return Optional.ofNullable(person)
                .map(p -> Person.builder()
                        .aliasPersonId(p.getAliasPersonId())
                        .attention(p.getAttention())
                        .birthPlace(p.getBirthPlace())
                        .careOf(p.getCareOf())
                        .citizenship(p.getCitizenship())
                        .comprehendEnglish(typesTransformer.ynToBoolean(p.getComprehendEnglishFlag()))
                        .coronerNumber(p.getCoronerNumber())
                        .criminalHistoryText(p.getCriminalHistoryText())
                        .dateOfBirth(typesTransformer.localDateOf(p.getBirthdate()))
                        .dateOfBirth(typesTransformer.localDateOf(p.getDeceasedDate()))
                        .employer(p.getEmployer())
                        .firstName(p.getFirstName())
                        .interpreterRequired(typesTransformer.ynToBoolean(p.getInterpreterRequired()))
                        .keepBiometrics(typesTransformer.ynToBoolean(p.getKeepBiometrics()))
                        .languageCode(p.getLanguageCode())
                        .lastName(p.getLastName())
                        .maritalStatus(p.getMaritalStatus())
                        .memoText(p.getMemoText())
                        .middleName(p.getMiddleName())
                        .nameType(p.getNameType())
                        .nameSequence(p.getNameSequence())
                        .occupationCode(p.getOccupationCode())
                        .personId(p.getPersonId())
                        .primaryLanguageCode(p.getPrimaryLanguageCode())
                        .profileCode(p.getProfileCode())
                        .remitter(typesTransformer.ynToBoolean(p.getRemitterFlag()))
                        .rootPersonId(p.getRootPersonId())
                        .sex(p.getSex())
                        .staff(typesTransformer.ynToBoolean(p.getStaffFlag()))
                        .suspended(typesTransformer.ynToBoolean(p.getSuspendedFlag()))
                        .suspendedDate(typesTransformer.localDateOf(p.getSuspendedDate()))
                        .title(p.getTitle())
                        .build())
                .orElse(null);
    }

    List<Address> addressesOf(final uk.gov.justice.digital.nomis.jpa.entity.Person person) {
        return Optional.ofNullable(person)
                .map(p -> p.getAddresses().stream()
                        .sorted(BY_ADDRESS_MODIFIED)
                        .map(addressesTransformer::addressOf)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }
}
