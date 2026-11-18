package cl.duocuc.siniestros.service;

import cl.duocuc.siniestros.client.CatalogoClient;
import cl.duocuc.siniestros.client.UsuarioClient;
import cl.duocuc.siniestros.dto.AsignarRequest;
import cl.duocuc.siniestros.dto.AvanzarRequest;
import cl.duocuc.siniestros.dto.CompaniaDto;
import cl.duocuc.siniestros.dto.GestionRequest;
import cl.duocuc.siniestros.dto.RegistrarSiniestroRequest;
import cl.duocuc.siniestros.dto.SiniestroResponse;
import cl.duocuc.siniestros.dto.TipoSiniestroDto;
import cl.duocuc.siniestros.dto.UsuarioDto;
import cl.duocuc.siniestros.model.Gestion;
import cl.duocuc.siniestros.model.Siniestro;
import cl.duocuc.siniestros.repository.GestionRepository;
import cl.duocuc.siniestros.repository.SiniestroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SiniestroService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final List<String> ETAPAS = List.of("RECIBIDO", "EVALUACION", "LIQUIDACION", "CERRADO");

    private final SiniestroRepository siniestroRepository;
    private final GestionRepository gestionRepository;
    private final CatalogoClient catalogoClient;
    private final UsuarioClient usuarioClient;

    public List<SiniestroResponse> listarPorAsegurado(String aseguradoId) {
        return siniestroRepository.findByAseguradoIdOrderByIdDesc(aseguradoId).stream()
                .map(this::aRespuesta)
                .toList();
    }

    public List<SiniestroResponse> listarPorLiquidador(String liquidadorId) {
        return siniestroRepository.findByLiquidadorIdOrderByIdDesc(liquidadorId).stream()
                .map(this::aRespuesta)
                .toList();
    }

    public List<SiniestroResponse> listarSinAsignar() {
        return siniestroRepository.findByLiquidadorIdIsNullAndEtapaOrderByIdAsc("RECIBIDO").stream()
                .map(this::aRespuesta)
                .toList();
    }

    public SiniestroResponse obtener(String id) {
        return aRespuesta(buscar(id));
    }

    public SiniestroResponse registrar(RegistrarSiniestroRequest datos) {
        if (datos.aseguradoId() == null) {
            throw invalido("Indica el asegurado que reporta");
        }
        UsuarioDto asegurado = usuarioClient.obtener(datos.aseguradoId());
        if (asegurado == null) {
            throw invalido("El asegurado no existe");
        }
        CompaniaDto compania = catalogoClient.obtenerCompania(texto(datos.companiaCodigo()));
        if (compania == null) {
            throw invalido("La compañía no existe");
        }
        if (!compania.activa()) {
            throw invalido("Esa compañía no está disponible");
        }
        TipoSiniestroDto tipo = catalogoClient.obtenerTipo(texto(datos.tipoCodigo()));
        if (tipo == null) {
            throw invalido("El tipo de siniestro no existe");
        }
        if (texto(datos.comuna()).isEmpty() || texto(datos.descripcion()).isEmpty()) {
            throw invalido("La comuna y la descripción son obligatorias");
        }
        String ahora = LocalDateTime.now().format(FECHA_HORA);
        Siniestro nuevo = new Siniestro();
        nuevo.setNumero(numeroPara(siniestroRepository.count() + 1));
        nuevo.setAseguradoId(asegurado.id());
        nuevo.setCompaniaCodigo(compania.codigo());
        nuevo.setTipoCodigo(tipo.codigo());
        nuevo.setFechaOcurrencia(texto(datos.fechaOcurrencia()));
        nuevo.setFechaReporte(LocalDate.now().format(FECHA));
        nuevo.setComuna(texto(datos.comuna()));
        nuevo.setDescripcion(texto(datos.descripcion()));
        nuevo.setHuboTerceros(datos.huboTerceros() != null && datos.huboTerceros());
        nuevo.setEtapa("RECIBIDO");
        nuevo.setUltimaActualizacion(ahora);
        Siniestro siniestro = siniestroRepository.save(nuevo);
        registrarGestion(siniestro.getId(), asegurado.nombre(),
                "Reporte registrado con número " + siniestro.getNumero(), true);
        return aRespuesta(siniestro);
    }

    public SiniestroResponse asignar(String id, AsignarRequest datos) {
        Siniestro siniestro = buscar(id);
        if ("CERRADO".equals(siniestro.getEtapa())) {
            throw invalido("No se puede asignar un caso cerrado");
        }
        UsuarioDto liquidador = usuarioClient.obtener(datos.liquidadorId());
        if (liquidador == null) {
            throw invalido("El liquidador no existe");
        }
        TipoSiniestroDto tipo = catalogoClient.obtenerTipo(siniestro.getTipoCodigo());
        boolean equipoVehiculos = "VEHICULOS".equalsIgnoreCase(liquidador.equipo());
        if (tipo != null && tipo.esVehiculo() != equipoVehiculos) {
            throw invalido("El caso debe asignarse a un liquidador del área correspondiente");
        }
        siniestro.setLiquidadorId(liquidador.id());
        if ("RECIBIDO".equals(siniestro.getEtapa())) {
            siniestro.setEtapa("EVALUACION");
        }
        siniestro.setUltimaActualizacion(LocalDateTime.now().format(FECHA_HORA));
        siniestroRepository.save(siniestro);
        registrarGestion(siniestro.getId(), "Analista de administración",
                "Caso asignado a " + liquidador.nombre(), true);
        return aRespuesta(siniestro);
    }

    public SiniestroResponse avanzar(String id, AvanzarRequest datos) {
        Siniestro siniestro = buscar(id);
        int indice = ETAPAS.indexOf(siniestro.getEtapa());
        if (indice < 0 || indice == ETAPAS.size() - 1) {
            throw invalido("El caso ya está cerrado");
        }
        String siguiente = ETAPAS.get(indice + 1);
        if ("CERRADO".equals(siguiente)) {
            if (texto(datos.resultado()).isEmpty() || texto(datos.mensajeCierre()).isEmpty()) {
                throw invalido("Para cerrar indica el resultado y el mensaje de cierre");
            }
            siniestro.setResultadoCierre(texto(datos.resultado()));
            siniestro.setMensajeCierre(texto(datos.mensajeCierre()));
        }
        siniestro.setEtapa(siguiente);
        siniestro.setUltimaActualizacion(LocalDateTime.now().format(FECHA_HORA));
        siniestroRepository.save(siniestro);
        registrarGestion(siniestro.getId(), "Equipo liquidador",
                "El caso avanzó a la etapa " + siguiente.toLowerCase(), true);
        return aRespuesta(siniestro);
    }

    public List<Gestion> listarGestiones(String siniestroId) {
        buscar(siniestroId);
        return gestionRepository.findBySiniestroIdOrderByIdAsc(siniestroId);
    }

    public Gestion agregarGestion(String siniestroId, GestionRequest datos) {
        buscar(siniestroId);
        if (texto(datos.descripcion()).isEmpty()) {
            throw invalido("La descripción de la gestión es obligatoria");
        }
        return registrarGestion(siniestroId,
                texto(datos.responsable()).isEmpty() ? "Equipo liquidador" : texto(datos.responsable()),
                texto(datos.descripcion()),
                datos.visibleAsegurado() == null || datos.visibleAsegurado());
    }

    private Gestion registrarGestion(String siniestroId, String responsable, String descripcion, boolean visible) {
        Gestion gestion = new Gestion();
        gestion.setSiniestroId(siniestroId);
        gestion.setFecha(LocalDateTime.now().format(FECHA_HORA));
        gestion.setResponsable(responsable);
        gestion.setDescripcion(descripcion);
        gestion.setVisibleAsegurado(visible);
        return gestionRepository.save(gestion);
    }

    private Siniestro buscar(String id) {
        return siniestroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Siniestro no encontrado"));
    }

    private String numeroPara(long correlativo) {
        String anio = String.valueOf(LocalDate.now().getYear()).substring(2);
        return "SIN-" + anio + "-" + String.format("%06d", correlativo + 4860);
    }

    private SiniestroResponse aRespuesta(Siniestro siniestro) {
        CompaniaDto compania = catalogoClient.obtenerCompania(siniestro.getCompaniaCodigo());
        TipoSiniestroDto tipo = catalogoClient.obtenerTipo(siniestro.getTipoCodigo());
        UsuarioDto asegurado = usuarioClient.obtener(siniestro.getAseguradoId());
        UsuarioDto liquidador = usuarioClient.obtener(siniestro.getLiquidadorId());
        return new SiniestroResponse(
                siniestro.getId(),
                siniestro.getNumero(),
                siniestro.getAseguradoId(),
                asegurado == null ? null : asegurado.nombre(),
                siniestro.getCompaniaCodigo(),
                compania == null ? null : compania.nombre(),
                compania == null ? null : compania.plazoDiasHabiles(),
                siniestro.getTipoCodigo(),
                tipo == null ? null : tipo.nombre(),
                siniestro.getFechaOcurrencia(),
                siniestro.getFechaReporte(),
                siniestro.getComuna(),
                siniestro.getDescripcion(),
                siniestro.isHuboTerceros(),
                siniestro.getEtapa(),
                siniestro.getLiquidadorId(),
                liquidador == null ? null : liquidador.nombre(),
                siniestro.getResultadoCierre(),
                siniestro.getMensajeCierre(),
                siniestro.getUltimaActualizacion());
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private ResponseStatusException invalido(String mensaje) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }
}
