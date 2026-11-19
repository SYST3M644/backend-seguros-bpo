package cl.duocuc.siniestros.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Service
public class ArchivoService {

    private static final List<String> PERMITIDOS = List.of(
            "image/jpeg", "image/png", "image/webp", "application/pdf");

    public byte[] leer(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se recibió ningún archivo");
        }
        String tipo = archivo.getContentType();
        if (tipo == null || !PERMITIDOS.contains(tipo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo se aceptan imágenes JPG, PNG, WEBP o archivos PDF");
        }
        try {
            return archivo.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo leer el archivo");
        }
    }

    public String extensionDe(String tipo) {
        return "application/pdf".equals(tipo) ? ".pdf" : ".jpg";
    }
}
