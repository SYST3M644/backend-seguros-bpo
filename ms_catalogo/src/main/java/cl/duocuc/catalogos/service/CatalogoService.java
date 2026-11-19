package cl.duocuc.catalogos.service;

import cl.duocuc.catalogos.dto.CompaniaRequest;
import cl.duocuc.catalogos.dto.TipoSiniestroRequest;
import cl.duocuc.catalogos.dto.TipoSiniestroResponse;
import cl.duocuc.catalogos.model.Compania;
import cl.duocuc.catalogos.model.TipoSiniestro;
import cl.duocuc.catalogos.repository.CompaniaRepository;
import cl.duocuc.catalogos.repository.TipoSiniestroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private static final int MAX_PLAZO = 60;

    private final CompaniaRepository companiaRepository;
    private final TipoSiniestroRepository tipoRepository;

    public List<Compania> listarCompanias() {
        return companiaRepository.findAll();
    }

    public Compania obtenerCompania(String codigo) {
        return companiaRepository.findByCodigo(codigo)
                .orElseThrow(() -> noEncontrado("Compañía no encontrada"));
    }

    public Compania crearCompania(CompaniaRequest datos) {
        String nombre = texto(datos.nombre());
        if (nombre.length() < 3 || nombre.length() > 80) {
            throw invalido("El nombre debe tener entre 3 y 80 caracteres");
        }
        int plazo = datos.plazoDiasHabiles() == null ? 0 : datos.plazoDiasHabiles();
        if (plazo < 1 || plazo > MAX_PLAZO) {
            throw invalido("El plazo debe estar entre 1 y " + MAX_PLAZO + " días hábiles");
        }
        String codigo = codigoDesde(nombre);
        if (companiaRepository.existsByCodigoIgnoreCase(codigo)) {
            throw repetido("Ya existe una compañía con ese nombre");
        }
        Compania compania = new Compania();
        compania.setCodigo(codigo);
        compania.setNombre(nombre);
        compania.setActiva(datos.activa() == null || datos.activa());
        compania.setPlazoDiasHabiles(plazo);
        return companiaRepository.save(compania);
    }

    public List<TipoSiniestroResponse> listarTipos() {
        return tipoRepository.findAll().stream().map(this::aRespuesta).toList();
    }

    public TipoSiniestroResponse obtenerTipo(String codigo) {
        return aRespuesta(tipoRepository.findByCodigo(codigo)
                .orElseThrow(() -> noEncontrado("Tipo de siniestro no encontrado")));
    }

    public TipoSiniestroResponse crearTipo(TipoSiniestroRequest datos) {
        String nombre = texto(datos.nombre());
        if (nombre.length() < 3 || nombre.length() > 80) {
            throw invalido("El nombre debe tener entre 3 y 80 caracteres");
        }
        String codigo = codigoDesde(nombre);
        if (tipoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw repetido("Ya existe un tipo de siniestro con ese nombre");
        }
        List<String> documentos = datos.documentos() == null ? List.of() : datos.documentos().stream()
                .map(String::trim)
                .filter(documento -> !documento.isEmpty())
                .toList();
        if (documentos.stream().anyMatch(documento -> documento.contains("|"))) {
            throw invalido("Los documentos no pueden incluir el signo |");
        }
        TipoSiniestro tipo = new TipoSiniestro();
        tipo.setCodigo(codigo);
        tipo.setNombre(nombre);
        tipo.setEsVehiculo(datos.esVehiculo() == null || datos.esVehiculo());
        tipo.setDocumentos(String.join("|", documentos));
        return aRespuesta(tipoRepository.save(tipo));
    }

    private TipoSiniestroResponse aRespuesta(TipoSiniestro tipo) {
        List<String> documentos = tipo.getDocumentos() == null || tipo.getDocumentos().isBlank()
                ? List.of()
                : List.of(tipo.getDocumentos().split("\\|"));
        return new TipoSiniestroResponse(
                tipo.getId(),
                tipo.getCodigo(),
                tipo.getNombre(),
                tipo.isEsVehiculo(),
                documentos);
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String codigoDesde(String nombre) {
        String sinTildes = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String codigo = sinTildes.toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return codigo.length() > 40 ? codigo.substring(0, 40) : codigo;
    }

    private ResponseStatusException noEncontrado(String mensaje) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, mensaje);
    }

    private ResponseStatusException invalido(String mensaje) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }

    private ResponseStatusException repetido(String mensaje) {
        return new ResponseStatusException(HttpStatus.CONFLICT, mensaje);
    }
}
