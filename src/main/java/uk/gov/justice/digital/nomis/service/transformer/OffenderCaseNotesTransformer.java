package uk.gov.justice.digital.nomis.service.transformer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.CaseNote;
import uk.gov.justice.digital.nomis.api.CaseNoteAmendment;
import uk.gov.justice.digital.nomis.api.KeyValue;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderCaseNote;
import uk.gov.justice.digital.nomis.jpa.entity.ReferenceCodePK;
import uk.gov.justice.digital.nomis.jpa.repository.ReferenceCodesRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class OffenderCaseNotesTransformer {
    private static final String NOTE_SOURCE = "NOTE_SOURCE";
    private static final String TASK_TYPE = "TASK_TYPE";
    private static final String TASK_SUBTYPE = "TASK_SUBTYPE";

    private static final String AMEND_REGEX = "\\.\\.\\.\\[(\\w+) updated the case notes* on ([0-9-/ :]*)\\]";
    private final static Pattern AMEND_CASE_NOTE_REGEX = Pattern.compile(AMEND_REGEX);

    private final TypesTransformer typesTransformer;
    private final ReferenceCodesRepository referenceCodesRepository;

    @Autowired
    public OffenderCaseNotesTransformer(
            final TypesTransformer typesTransformer,
            final ReferenceCodesRepository referenceCodesRepository) {
        this.typesTransformer = typesTransformer;
        this.referenceCodesRepository = referenceCodesRepository;
    }

    public CaseNote caseNoteOf(final OffenderCaseNote ocn) {
        final var amendments = amendmentsOf(ocn.getCaseNoteText(), ocn.getStaffId(), typesTransformer.localDateTimeOf(ocn.getCreationDate(), ocn.getCreationTime()));

        return CaseNote.builder()
                .caseNoteId(ocn.getCaseNoteId())
                .contactDateTime(typesTransformer.localDateTimeOf(ocn.getContactDate(), ocn.getContactTime()))
                .creationDateTime(typesTransformer.localDateTimeOf(ocn.getCreationDate(), ocn.getCreationTime()))
                .eventId(ocn.getEventId())
                .isAmendment(typesTransformer.ynToBoolean(ocn.getAmendmentFlag()))
                .isIwp(typesTransformer.ynToBoolean(ocn.getAmendmentFlag()))
                .offenderBookId(ocn.getOffenderBookId())
                .source(sourceOf(ocn.getNoteSourceCode()))
                .staffId(ocn.getStaffId())
                .subType(subTypeOf(ocn.getCaseNoteSubType()))
                .text(ocn.getCaseNoteText())
                .type(typeOf(ocn.getCaseNoteType()))
                .amendments(amendments)
                .build();
    }

    private KeyValue subTypeOf(final String caseNoteSubType) {
        return Optional.ofNullable(caseNoteSubType != null ?
                referenceCodesRepository.findById(ReferenceCodePK.builder()
                        .code(caseNoteSubType)
                        .domain(TASK_SUBTYPE)
                        .build()).orElse(null) : null)
                .map(rc -> KeyValue.builder().code(rc.getCode()).description(rc.getDescription()).build())
                .orElse(null);
    }

    private KeyValue typeOf(final String caseNoteType) {
        return Optional.ofNullable(caseNoteType != null ?
                referenceCodesRepository.findById(ReferenceCodePK.builder()
                        .code(caseNoteType)
                        .domain(TASK_TYPE)
                        .build()).orElse(null) : null)
                .map(rc -> KeyValue.builder().code(rc.getCode()).description(rc.getDescription()).build())
                .orElse(null);
    }

    private KeyValue sourceOf(final String noteSourceCode) {
        return Optional.ofNullable(noteSourceCode != null ?
                referenceCodesRepository.findById(ReferenceCodePK.builder()
                        .code(noteSourceCode)
                        .domain(NOTE_SOURCE)
                        .build()).orElse(null) : null)
                .map(rc -> KeyValue.builder().code(rc.getCode()).description(rc.getDescription()).build())
                .orElse(null);
    }

    private List<CaseNoteAmendment> amendmentsOf(final String caseNoteText, final Long staffId, final LocalDateTime creationDateTime) {
        final var breakUp = caseNoteText.split(AMEND_REGEX);
        var workingText = caseNoteText;

        final List<CaseNoteAmendment> amendments = new ArrayList<>();

        for (var amendmentCount = 0; amendmentCount < breakUp.length; amendmentCount++) {
            final var amendmentText = breakUp[amendmentCount];

            if (amendmentCount == 0) {
                amendments.add(CaseNoteAmendment.builder()
                        .text(amendmentText.trim())
                        .authorName(staffId.toString())
                        .creationDateTime(creationDateTime)
                        .build());

                workingText = StringUtils.replace(workingText, amendmentText, StringUtils.EMPTY, 1);
            } else {
                final var firstOcc = StringUtils.indexOf(workingText, amendmentText);

                final var amendmentDetails = StringUtils.substring(workingText, 0, firstOcc);
                final var m = AMEND_CASE_NOTE_REGEX.matcher(amendmentDetails);

                if (m.find()) {
                    amendments.add(CaseNoteAmendment.builder()
                            .text(amendmentText.trim())
                            .authorName(m.group(1))
                            .creationDateTime(typesTransformer.localDateTimeOf(m.group(2)))
                            .build());
                }

                workingText = StringUtils.substring(workingText, firstOcc);
            }
        }

        return amendments;
    }
}
