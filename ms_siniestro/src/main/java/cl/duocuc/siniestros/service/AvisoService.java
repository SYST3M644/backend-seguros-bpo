package cl.duocuc.siniestros.service;

import cl.duocuc.siniestros.model.Aviso;
import cl.duocuc.siniestros.model.Siniestro;
import cl.duocuc.siniestros.repository.AvisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvisoService {

    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final AvisoRepository avisoRepository;

    public List<Aviso> listar(String usuarioId) {
        return avisoRepository.findByDestinatarioIdOrderByIdDesc(usuarioId);
    }

    public long sinLeer(String usuarioId) {
        return avisoRepository.countByDestinatarioIdAndLeidoFalse(usuarioId);
    }

    public Aviso marcarLeido(String id) {
        Aviso aviso = avisoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El aviso no existe"));
        aviso.setLeido(true);
        return avisoRepository.save(aviso);
    }

    public void crear(Siniestro siniestro, String destinatarioId, String mensaje) {
        if (destinatarioId == null || destinatarioId.isBlank()) {
            return;
        }
        Aviso aviso = new Aviso();
        aviso.setSiniestroId(siniestro.getId());
        aviso.setNumero(siniestro.getNumero());
        aviso.setDestinatarioId(destinatarioId);
        aviso.setMensaje(mensaje);
        aviso.setFecha(LocalDateTime.now().format(FECHA_HORA));
        aviso.setLeido(false);
        avisoRepository.save(aviso);
    }
}
