package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.OffenderContactPerson;
import uk.gov.justice.digital.nomis.jpa.entity.AddressPhone;
import uk.gov.justice.digital.nomis.jpa.entity.AddressUsage;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.service.transformer.ContactPersonsTransformer;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;

@Service
@Transactional(readOnly = true)
public class ContactPersonsService {

    private final ContactPersonsTransformer contactPersonsTransformer;
    private final OffenderRepository offenderRepository;

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
                    Optional.ofNullable(address.getPrimaryFlag().equals("Y") ? Timestamp.valueOf("2999-12-31 00:00:00") : null).orElse(EARLIEST),
                    Optional.ofNullable(address.getModifyDatetime()).orElse(EARLIEST),
                    Optional.ofNullable(address.getCreateDatetime()).orElse(EARLIEST),
                    LATEST_MODIFIED_USAGE_OF.apply(address.getAddressUsages()),
                    LATEST_CREATED_USAGE_OF.apply(address.getAddressUsages()),
                    LATEST_MODIFIED_PHONE_OF.apply(address.getPhones()),
                    LATEST_CREATED_PHONE_OF.apply(address.getPhones())
            )
                    .max(comparing(Timestamp::getTime))
                    .get();

    private static final Function<uk.gov.justice.digital.nomis.jpa.entity.OffenderContactPerson, Timestamp> LAST_MODIFIED_CONTACT_OF = (uk.gov.justice.digital.nomis.jpa.entity.OffenderContactPerson person) ->
            Stream.of(
                    Optional.ofNullable(person.getModifyDatetime()).orElse(EARLIEST),
                    Optional.ofNullable(person.getCreateDatetime()).orElse(EARLIEST),
                    Optional.ofNullable(person.getPerson())
                            .map(p -> p.getAddresses().stream()
                                    .map(LAST_MODIFIED_ADDRESS_OF)
                                    .max(comparing(Timestamp::getTime))
                                    .orElse(EARLIEST))
                            .orElse(EARLIEST)
            )
                    .max(comparing(Timestamp::getTime))
                    .get();

    private static final Comparator<uk.gov.justice.digital.nomis.jpa.entity.OffenderContactPerson> BY_CONTACT_PRIORITY =
            Comparator.comparing(LAST_MODIFIED_CONTACT_OF)
                    .reversed();

    @Autowired
    public ContactPersonsService(ContactPersonsTransformer contactPersonsTransformer, OffenderRepository offenderRepository) {
        this.contactPersonsTransformer = contactPersonsTransformer;
        this.offenderRepository = offenderRepository;
    }

    public Optional<List<OffenderContactPerson>> contactPersonsForOffender(Long offenderId) {

        return offenderRepository.findById(offenderId)
                .map(offender -> offender.getOffenderBookings()
                        .stream()
                        .flatMap(offenderBooking -> offenderBooking.getOffenderContactPersons().stream())
                        .sorted(BY_CONTACT_PRIORITY)
                        .map(contactPersonsTransformer::offenderContactPersonOf)
                        .collect(Collectors.toList()));

    }


}
