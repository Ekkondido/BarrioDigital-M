package cl.duoc.barriodigital.catalog.config;

import cl.duoc.barriodigital.catalog.model.ProcedureType;
import cl.duoc.barriodigital.catalog.repository.ProcedureTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(ProcedureTypeRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.saveAll(List.of(
                    new ProcedureType(null, "PODA_ARBOLES", "Poda de Arboles en Via Publica",
                            "Poda de despeje por cables electricos, ramas en riesgo de caida o visibilidad vial.",
                            10, 10, "Cedula de identidad, foto del arbol, direccion exacta", true),
                    new ProcedureType(null, "LUMINARIA_PUBLICA", "Reparacion de Luminaria Publica",
                            "Reporte de focos apagados, intermitentes o postes danados en avenidas o pasajes.",
                            15, 15, "Numero de poste o direccion cercana, fotografia de referencia", true),
                    new ProcedureType(null, "BACHEO_CALLES", "Bacheo y Reparacion de Calzadas",
                            "Reparacion de hoyos en pavimento o veredas comunales que representen peligro.",
                            5, 5, "Ubicacion precisa, fotos de la calzada o vereda afectada", true),
                    new ProcedureType(null, "RETIRO_ESCOMBROS", "Retiro de Escombros y Enseres",
                            "Servicio municipal para coordinar tolvas o camion de recoleccion de enseres voluminosos.",
                            8, 8, "Comprobante de domicilio, listado de articulos a retirar", true),
                    new ProcedureType(null, "CONTROL_PLAGAS", "Fumigacion y Control de Plagas Comunal",
                            "Intervencion sanitaria en espacios publicos, plazas o zanjas comunales.",
                            6, 6, "Ubicacion, descripcion del foco detectado", true)
                ));
            }
        };
    }
}
