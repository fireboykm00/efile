package com.efile.core.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx", "xlsx", "png");
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;
    private final Path baseDirectory;

    public FileStorageService(FileStorageProperties properties) {
        String location = properties.getBasePath();
        this.baseDirectory = Paths.get(location).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.baseDirectory);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create base upload directory", ex);
        }
    }

    public String store(MultipartFile file, Long caseId) {
        validateFile(file);
        String extension = extractExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + "." + extension;
        LocalDate today = LocalDate.now();
        Path targetDirectory = baseDirectory.resolve(Paths.get(
            String.valueOf(today.getYear()),
            String.format("%02d", today.getMonthValue()),
            caseId == null ? "general" : caseId.toString()
        ));
        try {
            Files.createDirectories(targetDirectory);
            Path targetFile = targetDirectory.resolve(filename);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            Path relative = baseDirectory.relativize(targetFile);
            return relative.toString().replace('\\', '/');
        } catch (IOException ex) {
            throw new FileStorageException("Failed to store file", ex);
        }
    }

    public Resource loadAsResource(String storedPath) {
        try {
            Path absolutePath = baseDirectory.resolve(storedPath).normalize();
            if (!absolutePath.startsWith(baseDirectory)) {
                throw new FileStorageException("Invalid file path");
            }
            Resource resource = new UrlResource(absolutePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new FileStorageException("File not found");
            }
            return resource;
        } catch (IOException ex) {
            throw new FileStorageException("Could not read file", ex);
        }
    }

    public void delete(String storedPath) {
        try {
            Path absolutePath = baseDirectory.resolve(storedPath).normalize();
            if (!absolutePath.startsWith(baseDirectory)) {
                throw new FileStorageException("Invalid file path");
            }
            Files.deleteIfExists(absolutePath);
        } catch (IOException ex) {
            throw new FileStorageException("Failed to delete file", ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new FileStorageException("File exceeds maximum allowed size of 10MB");
        }
        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileStorageException("Unsupported file type");
        }
    }

    private String extractExtension(String originalFilename) {
        String filename = StringUtils.cleanPath(originalFilename == null ? "" : originalFilename);
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex == filename.length() - 1) {
            throw new FileStorageException("File must have a valid extension");
        }
        String extension = filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileStorageException("Unsupported file type");
        }
        return extension;
    }
}
