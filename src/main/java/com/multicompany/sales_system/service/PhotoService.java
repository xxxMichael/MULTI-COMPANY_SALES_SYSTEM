package com.multicompany.sales_system.service;

import com.multicompany.sales_system.dto.photo.PhotoRequestDTO;
import com.multicompany.sales_system.dto.photo.PhotoResponseDTO;

import java.util.List;

public interface PhotoService {

    PhotoResponseDTO createPhoto(PhotoRequestDTO photoRequestDTO);

    PhotoResponseDTO getPhotoById(Long id);

    List<PhotoResponseDTO> getAllPhotos();

    List<PhotoResponseDTO> getPhotosByProductId(Long productId);

    PhotoResponseDTO updatePhoto(Long id, PhotoRequestDTO photoRequestDTO);

    void deletePhoto(Long id);

    void deletePhotosByProductId(Long productId);
}