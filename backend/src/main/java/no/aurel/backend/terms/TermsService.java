package no.aurel.backend.terms;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.terms.dto.TermsSectionRequest;
import no.aurel.backend.terms.dto.TermsSectionResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class TermsService {

    private final TermsSectionRepository repository;

    @Transactional(readOnly = true)
    public List<TermsSectionResponse> findAll() {
        return repository.findAllByOrderBySortOrder().stream().map(TermsSectionResponse::from).toList();
    }

    // The admin edits the whole page and saves it in one go, so the list in the request simply replaces the old one.
    // The order of the list is the order on the page.
    public List<TermsSectionResponse> replaceAll(List<TermsSectionRequest> sections) {
        repository.deleteAllInBatch();
        List<TermsSection> saved = repository.saveAll(IntStream.range(0, sections.size())
                .mapToObj(i -> toEntity(sections.get(i), i))
                .toList());
        return saved.stream().map(TermsSectionResponse::from).toList();
    }

    private TermsSection toEntity(TermsSectionRequest request, int position) {
        String heading = request.heading() == null || request.heading().isBlank() ? null : request.heading().trim();
        TermsSection section = new TermsSection();
        section.setHeading(heading);
        section.setBody(request.body().trim());
        section.setSortOrder(position);
        return section;
    }
}
