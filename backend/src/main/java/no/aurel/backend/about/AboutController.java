package no.aurel.backend.about;

import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.aurel.backend.about.dto.AboutRequest;
import no.aurel.backend.about.dto.AboutResponse;

// GET is public (the website shows the box); changing anything is admin only (see SecurityConfig)
@RestController
@RequestMapping("/api/about")
@RequiredArgsConstructor
public class AboutController {

    private final AboutService service;

    @GetMapping
    public AboutResponse get() {
        return service.get();
    }

    @PutMapping
    public AboutResponse updateText(@Valid @RequestBody AboutRequest request) {
        return service.updateText(request);
    }

    @GetMapping("/image")
    public ResponseEntity<byte[]> image() {
        return service.findImage()
                .map(image -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(image.getContentType()))
                        // The address contains ?v=<upload time>, so a new photo is a new address: cache for long
                        .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                        .body(image.getData()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // The raw picture is the request body (the website sends it already shrunk), so no multipart handling is needed
    @PutMapping(value = "/image", consumes = MediaType.ALL_VALUE)
    public AboutResponse uploadImage(@RequestBody byte[] data) {
        return service.saveImage(data);
    }

    @DeleteMapping("/image")
    public AboutResponse deleteImage() {
        return service.deleteImage();
    }
}
