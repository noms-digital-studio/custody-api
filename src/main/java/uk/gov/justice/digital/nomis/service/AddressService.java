package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.Address;
import uk.gov.justice.digital.nomis.jpa.entity.AddressPhone;
import uk.gov.justice.digital.nomis.jpa.entity.AddressUsage;
import uk.gov.justice.digital.nomis.jpa.repository.AddressRepository;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.service.transformer.AddressesTransformer;

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
public class AddressService {

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

    private final AddressRepository addressRepository;
    private final OffenderRepository offenderRepository;
    private final AddressesTransformer addressesTransformer;

    @Autowired
    public AddressService(final AddressRepository addressRepository, final OffenderRepository offenderRepository, final AddressesTransformer addressesTransformer) {
        this.addressRepository = addressRepository;
        this.offenderRepository = offenderRepository;
        this.addressesTransformer = addressesTransformer;
    }

    @Transactional
    public Page<Address> getAddresses(final Pageable pageable) {
        final var rawAddressPage = addressRepository.findAll(pageable);

        final var addresses = rawAddressPage.getContent().stream()
                .map(addressesTransformer::addressOf)
                .collect(Collectors.toList());

        return new PageImpl<>(addresses, pageable, rawAddressPage.getTotalElements());
    }

    @Transactional
    public Optional<List<Address>> getOffenderAddresses(final Long offenderId) {

        return offenderRepository.findById(offenderId)
                .map(offender -> offender.getOffenderAddresses()
                        .stream()
                        .sorted(BY_ADDRESS_MODIFIED)
                        .map(addressesTransformer::addressOf)
                        .collect(Collectors.toList()));
    }

}
