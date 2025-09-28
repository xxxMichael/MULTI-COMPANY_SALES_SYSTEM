package com.multicompany.sales_system.service.impl;

import com.multicompany.sales_system.dto.photo.PhotoRequestDTO;
import com.multicompany.sales_system.dto.photo.PhotoResponseDTO;
import com.multicompany.sales_system.model.FotoProducto;
import com.multicompany.sales_system.model.Producto;
import com.multicompany.sales_system.repository.PhotoRepository;
import com.multicompany.sales_system.repository.ProductRepository;
import com.multicompany.sales_system.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PhotoServiceImpl implements PhotoService {

    private final PhotoRepository photoRepository;
    private final ProductRepository productRepository;

    @Override
    public PhotoResponseDTO createPhoto(PhotoRequestDTO photoRequestDTO) {
        Producto producto = productRepository.findById(photoRequestDTO.getIdProducto())
                .orElseThrow(() -> new RuntimeException(
                        "Producto no encontrado con ID: " + photoRequestDTO.getIdProducto()));

        FotoProducto foto = new FotoProducto();
        foto.setUrl(photoRequestDTO.getUrl());
        foto.setProducto(producto);

        FotoProducto savedPhoto = photoRepository.save(foto);
        return convertToResponseDTO(savedPhoto);
    }

    @Override
    @Transactional(readOnly = true)
    public PhotoResponseDTO getPhotoById(Long id) {
        FotoProducto foto = photoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Foto no encontrada con ID: " + id));
        return convertToResponseDTO(foto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotoResponseDTO> getAllPhotos() {
        return photoRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotoResponseDTO> getPhotosByProductId(Long productId) {
        List<FotoProducto> fotos = photoRepository.findByProductoIdProducto(productId);
        return fotos.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PhotoResponseDTO updatePhoto(Long id, PhotoRequestDTO photoRequestDTO) {
        FotoProducto foto = photoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Foto no encontrada con ID: " + id));

        Producto producto = productRepository.findById(photoRequestDTO.getIdProducto())
                .orElseThrow(() -> new RuntimeException(
                        "Producto no encontrado con ID: " + photoRequestDTO.getIdProducto()));

        foto.setUrl(photoRequestDTO.getUrl());
        foto.setProducto(producto);

        FotoProducto updatedPhoto = photoRepository.save(foto);
        return convertToResponseDTO(updatedPhoto);
    }

    @Override
    public void deletePhoto(Long id) {
        if (!photoRepository.existsById(id)) {
            throw new RuntimeException("Foto no encontrada con ID: " + id);
        }
        photoRepository.deleteById(id);
    }

    @Override
    public void deletePhotosByProductId(Long productId) {
        List<FotoProducto> fotos = photoRepository.findByProductoIdProducto(productId);
        photoRepository.deleteAll(fotos);
    }

    private PhotoResponseDTO convertToResponseDTO(FotoProducto foto) {
        PhotoResponseDTO dto = new PhotoResponseDTO();
        dto.setIdFoto(foto.getIdFoto());
        dto.setUrl(foto.getUrl());
        if (foto.getProducto() != null) {
            dto.setIdProducto(foto.getProducto().getIdProducto());
        }
        return dto;
    }
}