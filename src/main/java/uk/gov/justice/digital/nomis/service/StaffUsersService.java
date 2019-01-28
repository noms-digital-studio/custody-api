package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.NomisStaffUser;
import uk.gov.justice.digital.nomis.jpa.entity.StaffUserAccount;
import uk.gov.justice.digital.nomis.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.digital.nomis.service.transformer.StaffUserTransformer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StaffUsersService {

    private final StaffUserTransformer staffUserTransformer;
    private final StaffUserAccountRepository staffUserAccountRepository;

    @Autowired
    public StaffUsersService(StaffUserTransformer staffUserTransformer, StaffUserAccountRepository staffUserAccountRepository) {
        this.staffUserTransformer = staffUserTransformer;
        this.staffUserAccountRepository = staffUserAccountRepository;
    }


    public Optional<NomisStaffUser> staffUserForUsername(String username) {

        return staffUserAccountRepository.findById(username)
                .map(staffUserTransformer::userOf);
    }

    public Page<NomisStaffUser> getStaffUsers(Pageable pageable) {
        Page<StaffUserAccount> staffUsers = staffUserAccountRepository.findAll(pageable);

        List<NomisStaffUser> nomisStaffUserList = staffUsers.getContent()
                .stream()
                .map(staffUserTransformer::userOf)
                .collect(Collectors.toList());

        return new PageImpl<>(nomisStaffUserList, pageable, staffUsers.getTotalElements());
    }
}
