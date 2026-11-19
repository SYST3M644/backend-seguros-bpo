package cl.duocuc.catalogos.config;

import cl.duocuc.catalogos.model.Comuna;
import cl.duocuc.catalogos.model.Region;
import cl.duocuc.catalogos.repository.ComunaRepository;
import cl.duocuc.catalogos.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatosGeograficos implements CommandLineRunner {

    private final RegionRepository regionRepository;
    private final ComunaRepository comunaRepository;

    private int orden = 0;
    private final List<Region> regiones = new ArrayList<>();
    private final List<Comuna> comunas = new ArrayList<>();

    @Override
    public void run(String... args) {
        if (regionRepository.count() > 0) {
            return;
        }

        region("ARICA", "Arica y Parinacota",
                "Arica", "Camarones", "Putre", "General Lagos");

        region("TARAPACA", "Tarapacá",
                "Iquique", "Alto Hospicio", "Pozo Almonte", "Camiña", "Colchane", "Huara", "Pica");

        region("ANTOFAGASTA", "Antofagasta",
                "Antofagasta", "Mejillones", "Sierra Gorda", "Taltal", "Calama", "Ollagüe",
                "San Pedro de Atacama", "Tocopilla", "María Elena");

        region("ATACAMA", "Atacama",
                "Copiapó", "Caldera", "Tierra Amarilla", "Chañaral", "Diego de Almagro",
                "Vallenar", "Alto del Carmen", "Freirina", "Huasco");

        region("COQUIMBO", "Coquimbo",
                "La Serena", "Coquimbo", "Andacollo", "La Higuera", "Paiguano", "Vicuña",
                "Illapel", "Canela", "Los Vilos", "Salamanca", "Ovalle", "Combarbalá",
                "Monte Patria", "Punitaqui", "Río Hurtado");

        region("VALPARAISO", "Valparaíso",
                "Valparaíso", "Casablanca", "Concón", "Juan Fernández", "Puchuncaví", "Quintero",
                "Viña del Mar", "Isla de Pascua", "Los Andes", "Calle Larga", "Rinconada",
                "San Esteban", "La Ligua", "Cabildo", "Papudo", "Petorca", "Zapallar", "Quillota",
                "La Calera", "Hijuelas", "La Cruz", "Nogales", "San Antonio", "Algarrobo",
                "Cartagena", "El Quisco", "El Tabo", "Santo Domingo", "San Felipe", "Catemu",
                "Llaillay", "Panquehue", "Putaendo", "Santa María", "Quilpué", "Limache",
                "Olmué", "Villa Alemana");

        region("METROPOLITANA", "Metropolitana de Santiago",
                "Santiago", "Cerrillos", "Cerro Navia", "Conchalí", "El Bosque",
                "Estación Central", "Huechuraba", "Independencia", "La Cisterna", "La Florida",
                "La Granja", "La Pintana", "La Reina", "Las Condes", "Lo Barnechea", "Lo Espejo",
                "Lo Prado", "Macul", "Maipú", "Ñuñoa", "Pedro Aguirre Cerda", "Peñalolén",
                "Providencia", "Pudahuel", "Quilicura", "Quinta Normal", "Recoleta", "Renca",
                "San Joaquín", "San Miguel", "San Ramón", "Vitacura", "Puente Alto", "Pirque",
                "San José de Maipo", "Colina", "Lampa", "Tiltil", "San Bernardo", "Buin",
                "Calera de Tango", "Paine", "Melipilla", "Alhué", "Curacaví", "María Pinto",
                "San Pedro", "Talagante", "El Monte", "Isla de Maipo", "Padre Hurtado",
                "Peñaflor");

        region("OHIGGINS", "Libertador General Bernardo O'Higgins",
                "Rancagua", "Codegua", "Coinco", "Coltauco", "Doñihue", "Graneros", "Las Cabras",
                "Machalí", "Malloa", "Mostazal", "Olivar", "Peumo", "Pichidegua",
                "Quinta de Tilcoco", "Rengo", "Requínoa", "San Vicente", "Pichilemu",
                "La Estrella", "Litueche", "Marchihue", "Navidad", "Paredones", "San Fernando",
                "Chépica", "Chimbarongo", "Lolol", "Nancagua", "Palmilla", "Peralillo",
                "Placilla", "Pumanque", "Santa Cruz");

        region("MAULE", "Maule",
                "Talca", "Constitución", "Curepto", "Empedrado", "Maule", "Pelarco", "Pencahue",
                "Río Claro", "San Clemente", "San Rafael", "Cauquenes", "Chanco", "Pelluhue",
                "Curicó", "Hualañé", "Licantén", "Molina", "Rauco", "Romeral", "Sagrada Familia",
                "Teno", "Vichuquén", "Linares", "Colbún", "Longaví", "Parral", "Retiro",
                "San Javier", "Villa Alegre", "Yerbas Buenas");

        region("NUBLE", "Ñuble",
                "Chillán", "Bulnes", "Chillán Viejo", "El Carmen", "Pemuco", "Pinto", "Quillón",
                "San Ignacio", "Yungay", "Quirihue", "Cobquecura", "Coelemu", "Ninhue",
                "Portezuelo", "Ránquil", "Treguaco", "San Carlos", "Coihueco", "Ñiquén",
                "San Fabián", "San Nicolás");

        region("BIOBIO", "Biobío",
                "Concepción", "Coronel", "Chiguayante", "Florida", "Hualqui", "Lota", "Penco",
                "San Pedro de la Paz", "Santa Juana", "Talcahuano", "Tomé", "Hualpén", "Lebu",
                "Arauco", "Cañete", "Contulmo", "Curanilahue", "Los Álamos", "Tirúa",
                "Los Ángeles", "Antuco", "Cabrero", "Laja", "Mulchén", "Nacimiento", "Negrete",
                "Quilaco", "Quilleco", "San Rosendo", "Santa Bárbara", "Tucapel", "Yumbel",
                "Alto Biobío");

        region("ARAUCANIA", "La Araucanía",
                "Temuco", "Carahue", "Cunco", "Curarrehue", "Freire", "Galvarino", "Gorbea",
                "Lautaro", "Loncoche", "Melipeuco", "Nueva Imperial", "Padre Las Casas",
                "Perquenco", "Pitrufquén", "Pucón", "Saavedra", "Teodoro Schmidt", "Toltén",
                "Vilcún", "Villarrica", "Cholchol", "Angol", "Collipulli", "Curacautín",
                "Ercilla", "Lonquimay", "Los Sauces", "Lumaco", "Purén", "Renaico", "Traiguén",
                "Victoria");

        region("LOS_RIOS", "Los Ríos",
                "Valdivia", "Corral", "Lanco", "Los Lagos", "Máfil", "Mariquina", "Paillaco",
                "Panguipulli", "La Unión", "Futrono", "Lago Ranco", "Río Bueno");

        region("LOS_LAGOS", "Los Lagos",
                "Puerto Montt", "Calbuco", "Cochamó", "Fresia", "Frutillar", "Los Muermos",
                "Llanquihue", "Maullín", "Puerto Varas", "Castro", "Ancud", "Chonchi",
                "Curaco de Vélez", "Dalcahue", "Puqueldón", "Queilén", "Quellón", "Quemchi",
                "Quinchao", "Osorno", "Puerto Octay", "Purranque", "Puyehue", "Río Negro",
                "San Juan de la Costa", "San Pablo", "Chaitén", "Futaleufú", "Hualaihué",
                "Palena");

        region("AYSEN", "Aysén del General Carlos Ibáñez del Campo",
                "Coyhaique", "Lago Verde", "Aysén", "Cisnes", "Guaitecas", "Cochrane",
                "O'Higgins", "Tortel", "Chile Chico", "Río Ibáñez");

        region("MAGALLANES", "Magallanes y de la Antártica Chilena",
                "Punta Arenas", "Laguna Blanca", "Río Verde", "San Gregorio", "Cabo de Hornos",
                "Antártica", "Porvenir", "Primavera", "Timaukel", "Natales", "Torres del Paine");

        regionRepository.saveAll(regiones);
        comunaRepository.saveAll(comunas);
    }

    private void region(String codigo, String nombre, String... nombresComunas) {
        orden++;
        Region region = new Region();
        region.setCodigo(codigo);
        region.setNombre(nombre);
        region.setOrden(orden);
        regiones.add(region);

        for (String nombreComuna : nombresComunas) {
            Comuna comuna = new Comuna();
            comuna.setRegionCodigo(codigo);
            comuna.setNombre(nombreComuna);
            comunas.add(comuna);
        }
    }
}
