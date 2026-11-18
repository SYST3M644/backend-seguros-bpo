package cl.duocuc.siniestros.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class AlmacenamientoService {

    private static final List<String> PERMITIDOS = List.of(
            "image/jpeg", "image/png", "image/webp", "application/pdf");

    private final Path carpeta;

    public AlmacenamientoService(@Value("${app.evidencias.carpeta}") String carpeta) {
        this.carpeta = Path.of(carpeta).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.carpeta);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear la carpeta de evidencias", e);
        }
    }

    public String guardar(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se recibió ningún archivo");
        }
        String tipo = archivo.getContentType();
        if (tipo == null || !PERMITIDOS.contains(tipo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo se aceptan imágenes JPG, PNG, WEBP o archivos PDF");
        }
        String extension = tipo.equals("application/pdf") ? ".pdf" : ".jpg";
        String nombre = UUID.randomUUID() + extension;
        try {
            Files.copy(archivo.getInputStream(), carpeta.resolve(nombre),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo guardar el archivo");
        }
        return nombre;
    }

    public byte[] leer(String nombre) {
        try {
            return Files.readAllBytes(carpeta.resolve(nombre));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "El archivo no existe en el servidor");
        }
    }

    public void eliminar(String nombre) {
        if (nombre == null) {
            return;
        }
        try {
            Files.deleteIfExists(carpeta.resolve(nombre));
        } catch (IOException ignorada) {
        }
    }
}
