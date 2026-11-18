package cl.duocuc.siniestros.service;

import cl.duocuc.siniestros.dto.EvidenciaResponse;
import cl.duocuc.siniestros.model.Evidencia;
import cl.duocuc.siniestros.repository.EvidenciaRepository;
import cl.duocuc.siniestros.repository.SiniestroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvidenciaService {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final int MAXIMO_POR_CASO = 15;

    private final EvidenciaRepository evidenciaRepository;
    private final SiniestroRepository siniestroRepository;
    private final AlmacenamientoService almacenamiento;

    public List<EvidenciaResponse> listar(String siniestroId) {
        verificarSiniestro(siniestroId);
        return evidenciaRepository.findBySiniestroIdOrderByIdDesc(siniestroId).stream()
                .map(this::aRespuesta)
                .toList();
    }

    public byte[] leerArchivo(String id) {
        Evidencia evidencia = evidenciaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "La evidencia no existe"));
        return almacenamiento.leer(evidencia.getNombreArchivo());
    }

    public String mimeDe(String id) {
        return evidenciaRepository.findById(id)
                .map(Evidencia::getMime)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "La evidencia no existe"));
    }

    public EvidenciaResponse guardar(String siniestroId, String tipoDocumento, String subidoPor,
                                     MultipartFile archivo) {
        verificarSiniestro(siniestroId);
        String tipo = tipoDocumento == null ? "" : tipoDocumento.trim();
        if (tipo.isEmpty() || tipo.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Indica el tipo de documento, hasta 100 caracteres");
        }
        if (evidenciaRepository.countBySiniestroId(siniestroId) >= MAXIMO_POR_CASO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El siniestro ya tiene " + MAXIMO_POR_CASO + " archivos");
        }
        String nombre = almacenamiento.guardar(archivo);
        Evidencia evidencia = new Evidencia();
        evidencia.setSiniestroId(siniestroId);
        evidencia.setTipoDocumento(tipo);
        evidencia.setNombreArchivo(nombre);
        evidencia.setMime(archivo.getContentType());
        evidencia.setTamanoBytes((int) archivo.getSize());
        evidencia.setFechaRegistro(LocalDateTime.now().format(FECHA_HORA));
        evidencia.setSubidoPor(subidoPor == null || subidoPor.isBlank() ? "Asegurado" : subidoPor.trim());
        return aRespuesta(evidenciaRepository.save(evidencia));
    }

    public void eliminar(String id) {
        Evidencia evidencia = evidenciaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "La evidencia no existe"));
        almacenamiento.eliminar(evidencia.getNombreArchivo());
        evidenciaRepository.delete(evidencia);
    }

    private void verificarSiniestro(String siniestroId) {
        if (!siniestroRepository.existsById(siniestroId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Siniestro no encontrado");
        }
    }

    private EvidenciaResponse aRespuesta(Evidencia evidencia) {
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/siniestros/evidencias/{id}/archivo")
                .buildAndExpand(evidencia.getId())
                .toUriString();
        return new EvidenciaResponse(
                evidencia.getId(),
                evidencia.getSiniestroId(),
                evidencia.getTipoDocumento(),
                evidencia.getNombreArchivo(),
                evidencia.getMime(),
                evidencia.getTamanoBytes(),
                evidencia.getFechaRegistro(),
                evidencia.getSubidoPor(),
                url);
    }
}
