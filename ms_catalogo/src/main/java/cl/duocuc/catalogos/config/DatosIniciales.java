package cl.duocuc.catalogos.config;

import cl.duocuc.catalogos.model.Compania;
import cl.duocuc.catalogos.model.TipoSiniestro;
import cl.duocuc.catalogos.repository.CompaniaRepository;
import cl.duocuc.catalogos.repository.TipoSiniestroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final CompaniaRepository companiaRepository;
    private final TipoSiniestroRepository tipoRepository;

    @Override
    public void run(String... args) {
        if (companiaRepository.count() == 0) {
            companiaRepository.saveAll(List.of(
                    compania("CORDILLERA", "Seguros Cordillera", true, 10),
                    compania("LITORAL", "Seguros Litoral", true, 12),
                    compania("PATAGONIA", "Aseguradora Patagonia Sur", true, 10),
                    compania("VALLE_CENTRAL", "Seguros Valle Central", true, 8),
                    compania("PROTEGE", "Protege Seguros (no disponible)", false, 12)
            ));
        }
        if (tipoRepository.count() == 0) {
            tipoRepository.saveAll(List.of(
                    tipo("CHOQUE", "Vehículo · choque o colisión", true,
                            "Fotografías de los daños|Constancia policial (si hubo terceros)|Presupuesto de reparación"),
                    tipo("ROBO_VEHICULO", "Vehículo · robo", true,
                            "Constancia de denuncia policial|Copia de llaves (declaración)|Padrón del vehículo"),
                    tipo("CRISTALES", "Vehículo · rotura de cristales", true,
                            "Fotografías del cristal|Presupuesto o boleta de reposición"),
                    tipo("DANO_AGUA", "Hogar · daño por agua", false,
                            "Fotografías de los daños|Informe o boleta del gasfíter|Presupuesto de reparación"),
                    tipo("ROBO_HOGAR", "Hogar · robo", false,
                            "Constancia de denuncia policial|Listado de especies|Boletas o facturas de lo robado"),
                    tipo("INCENDIO", "Hogar · incendio", false,
                            "Fotografías de los daños|Certificado de Bomberos|Presupuesto de reparación")
            ));
        }
    }

    private Compania compania(String codigo, String nombre, boolean activa, int plazo) {
        Compania compania = new Compania();
        compania.setCodigo(codigo);
        compania.setNombre(nombre);
        compania.setActiva(activa);
        compania.setPlazoDiasHabiles(plazo);
        return compania;
    }

    private TipoSiniestro tipo(String codigo, String nombre, boolean esVehiculo, String documentos) {
        TipoSiniestro tipo = new TipoSiniestro();
        tipo.setCodigo(codigo);
        tipo.setNombre(nombre);
        tipo.setEsVehiculo(esVehiculo);
        tipo.setDocumentos(documentos);
        return tipo;
    }
}
