package no.aurel.backend.about;

import java.time.Clock;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.about.dto.AboutRequest;
import no.aurel.backend.about.dto.AboutResponse;
import no.aurel.backend.image.ImageFormat;

@Service
@RequiredArgsConstructor
@Transactional
public class AboutService {

    private final AboutContentRepository contentRepository;
    private final AboutImageRepository imageRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public AboutResponse get() {
        return AboutResponse.from(load());
    }

    public AboutResponse updateText(AboutRequest request) {
        AboutContent content = load();
        content.setHeading(request.heading().trim());
        content.setBody(request.body().trim());
        return AboutResponse.from(content);
    }

    @Transactional(readOnly = true)
    public Optional<AboutImage> findImage() {
        return imageRepository.findById(AboutContent.ID);
    }

    public AboutResponse saveImage(byte[] data) {
        String contentType = ImageFormat.requireValid(data);

        AboutImage image = imageRepository.findById(AboutContent.ID).orElseGet(AboutImage::new);
        image.setContentType(contentType);
        image.setData(data);
        imageRepository.save(image);

        AboutContent content = load();
        content.setImageVersion(clock.millis());
        return AboutResponse.from(content);
    }

    // Back to the default photo
    public AboutResponse deleteImage() {
        imageRepository.findById(AboutContent.ID).ifPresent(imageRepository::delete);
        AboutContent content = load();
        content.setImageVersion(null);
        return AboutResponse.from(content);
    }

    // The row is created by the migration, so it must exist
    private AboutContent load() {
        return contentRepository.findById(AboutContent.ID)
                .orElseThrow(() -> new IllegalStateException("about_content row is missing"));
    }
}
