package cl.duocuc.siniestros.service;

import cl.duocuc.siniestros.dto.EvidenciaResponse;
import cl.duocuc.siniestros.model.ArchivoEvidencia;
import cl.duocuc.siniestros.model.Evidencia;
import cl.duocuc.siniestros.model.Siniestro;
import cl.duocuc.siniestros.repository.ArchivoEvidenciaRepository;
import cl.duocuc.siniestros.repository.EvidenciaRepository;
import cl.duocuc.siniestros.repository.SiniestroRepository;
import cl.duocuc.siniestros.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvidenciaService {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final int MAXIMO_POR_CASO = 15;

    private final EvidenciaRepository evidenciaRepository;
    private final SiniestroRepository siniestroRepository;
    private final ArchivoService archivoService;
    private final ArchivoEvidenciaRepository archivoEvidenciaRepository;
    private final SolicitudRepository solicitudRepository;
    private final AvisoService avisoService;

    private void avisarAlLiquidador(String siniestroId, String tipoDocumento) {
        Siniestro siniestro = siniestroRepository.findById(siniestroId).orElse(null);
        if (siniestro == null || siniestro.getLiquidadorId() == null) {
            return;
        }

        solicitudRepository.findBySiniestroIdOrderByIdDesc(siniestroId).stream()
                .filter(solicitud -> "PENDIENTE".equals(solicitud.getEstado()))
                .filter(solicitud -> solicitud.getTipoDocumento().equalsIgnoreCase(tipoDocumento))
                .findFirst()
                .ifPresent(solicitud -> {
                    solicitud.setEstado("RECIBIDA");
                    solicitudRepository.save(solicitud);
                });

        avisoService.crear(siniestro, siniestro.getLiquidadorId(),
                "El asegurado envió: " + tipoDocumento);
    }

    public List<EvidenciaResponse> listar(String siniestroId) {
        verificarSiniestro(siniestroId);
        return evidenciaRepository.findBySiniestroIdOrderByIdDesc(siniestroId).stream()
                .map(this::aRespuesta)
                .toList();
    }

    public byte[] leerArchivo(String id) {
        return archivoEvidenciaRepository.findById(id)
                .map(ArchivoEvidencia::getContenido)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "La evidencia no existe"));
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
        byte[] contenido = archivoService.leer(archivo);
        String nombre = UUID.randomUUID() + archivoService.extensionDe(archivo.getContentType());

        Evidencia evidencia = new Evidencia();
        evidencia.setSiniestroId(siniestroId);
        evidencia.setTipoDocumento(tipo);
        evidencia.setNombreArchivo(nombre);
        evidencia.setMime(archivo.getContentType());
        evidencia.setTamanoBytes((int) archivo.getSize());
        evidencia.setFechaRegistro(LocalDateTime.now().format(FECHA_HORA));
        evidencia.setSubidoPor(subidoPor == null || subidoPor.isBlank() ? "Asegurado" : subidoPor.trim());
        Evidencia registrada = evidenciaRepository.save(evidencia);

        ArchivoEvidencia guardado = new ArchivoEvidencia();
        guardado.setId(registrada.getId());
        guardado.setContenido(contenido);
        archivoEvidenciaRepository.save(guardado);

        EvidenciaResponse respuesta = aRespuesta(registrada);
        avisarAlLiquidador(siniestroId, tipo);
        return respuesta;
    }

    public void eliminar(String id) {
        Evidencia evidencia = evidenciaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "La evidencia no existe"));
        archivoEvidenciaRepository.deleteById(evidencia.getId());
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
