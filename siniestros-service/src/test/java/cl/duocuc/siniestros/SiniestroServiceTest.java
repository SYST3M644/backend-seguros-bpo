package cl.duocuc.siniestros;

import cl.duocuc.siniestros.client.CatalogoClient;
import cl.duocuc.siniestros.client.UsuarioClient;
import cl.duocuc.siniestros.dto.AsignarRequest;
import cl.duocuc.siniestros.dto.AvanzarRequest;
import cl.duocuc.siniestros.dto.CompaniaDto;
import cl.duocuc.siniestros.dto.RegistrarSiniestroRequest;
import cl.duocuc.siniestros.dto.SiniestroResponse;
import cl.duocuc.siniestros.dto.TipoSiniestroDto;
import cl.duocuc.siniestros.dto.UsuarioDto;
import cl.duocuc.siniestros.model.Siniestro;
import cl.duocuc.siniestros.repository.GestionRepository;
import cl.duocuc.siniestros.repository.SiniestroRepository;
import cl.duocuc.siniestros.service.SiniestroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SiniestroServiceTest {

    private SiniestroRepository siniestros;
    private CatalogoClient catalogos;
    private UsuarioClient usuarios;
    private SiniestroService servicio;

    @BeforeEach
    void preparar() {
        siniestros = mock(SiniestroRepository.class);
        catalogos = mock(CatalogoClient.class);
        usuarios = mock(UsuarioClient.class);
        servicio = new SiniestroService(
                siniestros, mock(GestionRepository.class), catalogos, usuarios);

        when(siniestros.save(any(Siniestro.class)))
                .thenAnswer(llamada -> {
                    Siniestro guardado = llamada.getArgument(0);
                    if (guardado.getId() == null) {
                        guardado.setId("1");
                    }
                    return guardado;
                });
        when(catalogos.obtenerCompania(anyString()))
                .thenReturn(new CompaniaDto("1", "CORDILLERA", "Seguros Cordillera", true, 10));
        when(catalogos.obtenerTipo(anyString()))
                .thenReturn(new TipoSiniestroDto("1", "CHOQUE", "Vehiculo choque", true, List.of()));
        when(usuarios.obtener(anyString()))
                .thenReturn(new UsuarioDto("1", "Camila Rojas Fuentes", "crojas@correo.cl",
                        "15.842.371-5", null, "1", "Asegurado", null));
    }

    private Siniestro casoRecibido() {
        Siniestro caso = new Siniestro();
        caso.setId("1");
        caso.setNumero("SIN-26-004861");
        caso.setAseguradoId("1");
        caso.setCompaniaCodigo("CORDILLERA");
        caso.setTipoCodigo("CHOQUE");
        caso.setFechaOcurrencia("10-11-2026");
        caso.setFechaReporte("12-11-2026");
        caso.setComuna("Quilicura");
        caso.setDescripcion("Colision leve");
        caso.setHuboTerceros(false);
        caso.setEtapa("RECIBIDO");
        caso.setUltimaActualizacion("12-11-2026 10:00");
        return caso;
    }

    @Test
    void registrar_generaElNumeroDelSiniestro() {
        SiniestroResponse nuevo = servicio.registrar(new RegistrarSiniestroRequest(
                "1", "CORDILLERA", "CHOQUE", "10-11-2026",
                "Quilicura", "Colision en el estacionamiento", false));

        assertTrue(nuevo.numero().startsWith("SIN-"));
        assertEquals("RECIBIDO", nuevo.etapa());
    }

    @Test
    void registrar_conCompaniaInactiva_lanza400() {
        when(catalogos.obtenerCompania("PROTEGE"))
                .thenReturn(new CompaniaDto("5", "PROTEGE", "Protege Seguros", false, 12));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.registrar(new RegistrarSiniestroRequest(
                        "1", "PROTEGE", "CHOQUE", "10-11-2026",
                        "Maipu", "Dano por agua", false)));

        assertEquals(400, error.getStatusCode().value());
        assertTrue(error.getReason().contains("no esta disponible")
                || error.getReason().contains("no está disponible"));
    }

    @Test
    void asignar_aLiquidadorDeOtraArea_lanza400() {
        when(siniestros.findById("1")).thenReturn(Optional.of(casoRecibido()));
        when(usuarios.obtener("11")).thenReturn(new UsuarioDto("11", "Pedro C. Rivas",
                "privas@segurosbpo.cl", "", "HOGAR", "2", "Liquidador", null));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.asignar("1", new AsignarRequest("11")));

        assertEquals(400, error.getStatusCode().value());
    }

    @Test
    void asignar_aLiquidadorDelAreaCorrecta_dejaElCasoEnEvaluacion() {
        when(siniestros.findById("1")).thenReturn(Optional.of(casoRecibido()));
        when(usuarios.obtener("9")).thenReturn(new UsuarioDto("9", "Maria P. Lagos",
                "mlagos@segurosbpo.cl", "", "VEHICULOS", "2", "Liquidador", null));

        SiniestroResponse asignado = servicio.asignar("1", new AsignarRequest("9"));

        assertEquals("EVALUACION", asignado.etapa());
        assertEquals("9", asignado.liquidadorId());
    }

    @Test
    void avanzar_haciaElCierreSinMensaje_lanza400() {
        Siniestro enLiquidacion = casoRecibido();
        enLiquidacion.setEtapa("LIQUIDACION");
        when(siniestros.findById("1")).thenReturn(Optional.of(enLiquidacion));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.avanzar("1", new AvanzarRequest("", "")));

        assertEquals(400, error.getStatusCode().value());
    }
}
