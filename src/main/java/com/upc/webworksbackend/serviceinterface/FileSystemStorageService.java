package com.upc.webworksbackend.serviceinterface;

import com.upc.webworksbackend.exception.BusinessRuleException;
import com.upc.webworksbackend.exception.NotFoundException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class FileSystemStorageService {

    @Value("${media.location}")
    private String mediaLocation;

    private Path rootLocation;

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            ".png", ".jpg", ".jpeg", ".webp", ".gif", ".pdf"
    );

    @PostConstruct
    public void init() throws IOException {
        rootLocation = Paths.get(mediaLocation).toAbsolutePath().normalize();
        Files.createDirectories(rootLocation);
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("El archivo a subir no puede estar vacío.");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessRuleException("Nombre de archivo no válido.");
        }

        String cleanName = Paths.get(originalFilename).getFileName().toString();
        int dotIndex = cleanName.lastIndexOf('.');
        if (dotIndex == -1) {
            throw new BusinessRuleException("El archivo debe tener una extensión válida (.png, .jpg, .jpeg, .webp, .gif, .pdf).");
        }
        String extension = cleanName.substring(dotIndex).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessRuleException("Extensión de archivo no permitida. Solo se admiten imágenes (png, jpg, jpeg, webp, gif) y documentos pdf.");
        }

        String generatedFilename = UUID.randomUUID().toString() + extension;
        Path destinationFile = rootLocation.resolve(generatedFilename).normalize().toAbsolutePath();

        if (!destinationFile.startsWith(rootLocation)) {
            throw new BusinessRuleException("Ruta de archivo no permitida fuera del directorio de medios.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return generatedFilename;
        } catch (IOException e) {
            throw new RuntimeException("Error al almacenar el archivo", e);
        }
    }

    public Resource loadResource(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new NotFoundException("Nombre de archivo no proporcionado.");
        }
        try {
            Path file = rootLocation.resolve(fileName).normalize().toAbsolutePath();
            if (!file.startsWith(rootLocation)) {
                throw new NotFoundException("Archivo no encontrado: " + fileName);
            }
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new NotFoundException("Archivo no encontrado: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new NotFoundException("Archivo no encontrado: " + fileName);
        }
    }
}
