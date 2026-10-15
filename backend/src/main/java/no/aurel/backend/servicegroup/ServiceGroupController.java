package no.aurel.backend.servicegroup;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.aurel.backend.servicegroup.dto.ServiceGroupOrderRequest;
import no.aurel.backend.servicegroup.dto.ServiceGroupRequest;
import no.aurel.backend.servicegroup.dto.ServiceGroupResponse;

// The boxes on the website. Reading the list and the photos is public (SecurityConfig); everything else is admin only.
@RestController
@RequestMapping("/api/service-groups")
@RequiredArgsConstructor
public class ServiceGroupController {

    private final ServiceGroupService service;

    @GetMapping
    public List<ServiceGroupResponse> findAll() {
        return service.findAll();
    }

    @PostMapping
    public ResponseEntity<ServiceGroupResponse> create(@Valid @RequestBody ServiceGroupRequest request) {
        ServiceGroupResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/service-groups/" + created.id())).body(created);
    }

    // A fixed path, so it is never mistaken for PUT /{id}
    @PutMapping("/order")
    public List<ServiceGroupResponse> reorder(@Valid @RequestBody ServiceGroupOrderRequest request) {
        return service.reorder(request.ids());
    }

    @PutMapping("/{id}")
    public ServiceGroupResponse update(@PathVariable Long id, @Valid @RequestBody ServiceGroupRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable Long id) {
        return service.findImage(id)
                .map(image -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(image.getContentType()))
                        // The address contains ?v=<upload time>, so a new photo is a new address: cache for long
                        .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                        .body(image.getData()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // The raw picture is the request body (the website sends it already shrunk), so no multipart handling is needed
    @PutMapping(value = "/{id}/image", consumes = MediaType.ALL_VALUE)
    public ServiceGroupResponse uploadImage(@PathVariable Long id, @RequestBody byte[] data) {
        return service.saveImage(id, data);
    }

    @DeleteMapping("/{id}/image")
    public ServiceGroupResponse deleteImage(@PathVariable Long id) {
        return service.deleteImage(id);
    }
}
