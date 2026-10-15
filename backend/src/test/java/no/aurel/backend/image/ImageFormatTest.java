package no.aurel.backend.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class ImageFormatTest {

    @Test
    void recognisesJpeg() {
        assertEquals(Optional.of("image/jpeg"),
                ImageFormat.detect(new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}));
    }

    @Test
    void recognisesPng() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        assertEquals(Optional.of("image/png"), ImageFormat.detect(png));
    }

    @Test
    void recognisesWebp() {
        byte[] webp = "RIFF....WEBPVP8 ".getBytes(StandardCharsets.ISO_8859_1);
        assertEquals(Optional.of("image/webp"), ImageFormat.detect(webp));
    }

    @Test
    void refusesSvgHtmlAndEmptyFiles() {
        assertTrue(ImageFormat.detect("<svg xmlns=\"http://www.w3.org/2000/svg\"/>".getBytes(StandardCharsets.UTF_8)).isEmpty());
        assertTrue(ImageFormat.detect("<html><script>alert(1)</script>".getBytes(StandardCharsets.UTF_8)).isEmpty());
        assertTrue(ImageFormat.detect(new byte[0]).isEmpty());
    }
}
