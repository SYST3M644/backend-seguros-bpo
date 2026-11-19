package cl.duocuc.usuarios.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Service
public class ImagenService {

    private static final long MAXIMO = 2L * 1024 * 1024;

    public byte[] leer(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se recibió ninguna imagen");
        }
        String tipo = archivo.getContentType();
        if (tipo == null || !tipo.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo debe ser una imagen");
        }
        if (archivo.getSize() > MAXIMO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen no puede pesar más de 2 MB");
        }
        try {
            return archivo.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo leer la imagen");
        }
    }
}
