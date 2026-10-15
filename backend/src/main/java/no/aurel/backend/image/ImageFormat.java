package no.aurel.backend.image;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// Rules for pictures the admin uploads (the "Om oss" photo and the photo on each treatment box).
// What kind of picture a file really is, is worked out from its first bytes, never from the file name or the
// Content-Type header the browser sent. Only formats that cannot carry scripts are accepted (no SVG).
public final class ImageFormat {

    // The website shrinks photos before uploading, so a real photo is far below this
    public static final int MAX_BYTES = 3 * 1024 * 1024;

    private ImageFormat() {
    }

    // The content type of a valid upload, or a 400 with a message the admin can read
    public static String requireValid(byte[] data) {
        if (data.length == 0 || data.length > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bildet må være mindre enn 3 MB");
        }
        return detect(data).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bildet må være JPG, PNG eller WebP"));
    }

    public static Optional<String> detect(byte[] data) {
        if (data.length >= 3 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF) {
            return Optional.of("image/jpeg");
        }
        if (data.length >= 8 && (data[0] & 0xFF) == 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {
            return Optional.of("image/png");
        }
        // WebP: "RIFF" + 4 size bytes + "WEBP"
        if (data.length >= 12 && data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
                && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P') {
            return Optional.of("image/webp");
        }
        return Optional.empty();
    }
}
