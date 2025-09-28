package com.multicompany.sales_system.controller;

import com.multicompany.sales_system.dto.photo.PhotoRequestDTO;
import com.multicompany.sales_system.dto.photo.PhotoResponseDTO;
import com.multicompany.sales_system.service.PhotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping
    public ResponseEntity<PhotoResponseDTO> createPhoto(@Valid @RequestBody PhotoRequestDTO photoRequestDTO) {
        PhotoResponseDTO createdPhoto = photoService.createPhoto(photoRequestDTO);
        return new ResponseEntity<>(createdPhoto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhotoResponseDTO> getPhotoById(@PathVariable Long id) {
        PhotoResponseDTO photo = photoService.getPhotoById(id);
        return ResponseEntity.ok(photo);
    }

    @GetMapping
    public ResponseEntity<List<PhotoResponseDTO>> getAllPhotos() {
        List<PhotoResponseDTO> photos = photoService.getAllPhotos();
        return ResponseEntity.ok(photos);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<PhotoResponseDTO>> getPhotosByProductId(@PathVariable Long productId) {
        List<PhotoResponseDTO> photos = photoService.getPhotosByProductId(productId);
        return ResponseEntity.ok(photos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhotoResponseDTO> updatePhoto(
            @PathVariable Long id,
            @Valid @RequestBody PhotoRequestDTO photoRequestDTO) {
        PhotoResponseDTO updatedPhoto = photoService.updatePhoto(id, photoRequestDTO);
        return ResponseEntity.ok(updatedPhoto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        photoService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/product/{productId}")
    public ResponseEntity<Void> deletePhotosByProductId(@PathVariable Long productId) {
        photoService.deletePhotosByProductId(productId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}