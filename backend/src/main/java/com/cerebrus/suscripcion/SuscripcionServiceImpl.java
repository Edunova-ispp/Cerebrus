package com.cerebrus.suscripcion;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cerebrus.suscripcion.dto.PlanPreciosDTO;
import com.cerebrus.suscripcion.dto.SuscripcionDTO;
import com.cerebrus.suscripcion.dto.SuscripcionRequest;
import com.cerebrus.suscripcion.mapper.SuscripcionMapper;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.organizacion.Organizacion;
import com.cerebrus.usuario.organizacion.OrganizacionRepository;


@Service
@Transactional
public class SuscripcionServiceImpl implements SuscripcionService {

    private final SuscripcionRepository suscripcionRepository;
    private final OrganizacionRepository organizacionRepository;
    private final UsuarioService usuarioService;

    @Autowired
    public SuscripcionServiceImpl(SuscripcionRepository suscripcionRepository, OrganizacionRepository organizacionRepository, UsuarioService usuarioService) {
        this.suscripcionRepository = suscripcionRepository;
        this.organizacionRepository = organizacionRepository;
        this.usuarioService = usuarioService;
    }

    private static final Map<String, Integer> limitesBase = Map.of(
        "profesor", 20,
        "alumno", 50
    );

    private static final Map<String, Double> preciosBase = Map.of(
        "profesor", 10.0,
        "alumno", 5.0,
        "profesor_extra", 7.0,
        "alumno_extra", 3.0
    );


    @Override
    public List<SuscripcionDTO> obtenerSuscripcionesOrganizacion(Long organizacionId) {
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Organizacion)) {
            throw new AccessDeniedException("Solo una organización puede acceder a sus suscripciones");
        }else{
            Organizacion organizacion = organizacionRepository.findById(organizacionId)
                .orElseThrow(() -> new IllegalArgumentException("Organización no encontrada"));

            if(!u.getId().equals(organizacionId)) {
                throw new AccessDeniedException("No se puede acceder a las suscripciones de una organización diferente a la del usuario logueado");
            } else if (organizacion.getSuscripciones().isEmpty()) {
                throw new IllegalArgumentException("La organización no tiene suscripciones, contrata una para acceder a las funcionalidades de Cerebrus");
            } else {
                List<SuscripcionDTO> suscripcionesOrganizacion =  organizacion.getSuscripciones().stream()
                                                            .map(SuscripcionMapper::toSuscripcionDTO)
                                                            .toList();
                return suscripcionesOrganizacion;
            }
        }
    }

    @Override
    public SuscripcionDTO obtenerSuscripcionOrganizacion(Long organizacionId, Long suscripcionId) {
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Organizacion)) {
            throw new AccessDeniedException("Solo una organización puede acceder a su suscripción");
        }else{
            Suscripcion suscripcion = suscripcionRepository.findByIdAndOrganizacionId(suscripcionId, organizacionId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la suscripción de la organización"));

            if(!u.getId().equals(organizacionId) || !suscripcion.getOrganizacion().getId().equals(organizacionId)) {
                throw new AccessDeniedException("No se puede acceder a una suscripción de una organización diferente a la del usuario logueado");
            } else {
                return SuscripcionMapper.toSuscripcionDTO(suscripcion);
            }
        }
    }

    @Override
    public SuscripcionDTO obtenerSuscripcionActivaOrganizacion(Long organizacionId) {
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Organizacion)) {
            throw new AccessDeniedException("Solo una organización puede acceder a su suscripción activa");
        } else {
            Organizacion organizacion = organizacionRepository.findById(organizacionId)
                .orElseThrow(() -> new IllegalArgumentException("Organización no encontrada"));
            
            if(!u.getId().equals(organizacionId)) {
                throw new AccessDeniedException("No se puede acceder a una suscripción de una organización diferente a la del usuario logueado");
            }else{
                Integer numSuscripciones = organizacion.getSuscripciones().size();
                if(organizacion.getActivo()) {
                    Suscripcion suscripcion = organizacion.getSuscripciones().get(numSuscripciones - 1);
                    return SuscripcionMapper.toSuscripcionDTO(suscripcion);
                } else {
                    throw new IllegalArgumentException("No se encontró una suscripción activa para la organización, contrata una nueva para acceder a las funcionalidades de Cerebrus");
                }
            }
            
        }
    }

    @Override
    public PlanPreciosDTO obtenerPlanPrecios() {
        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Organizacion)) {
            throw new AccessDeniedException("Solo una organización puede acceder a los planes de precios");
        } else {
            return new PlanPreciosDTO(limitesBase, preciosBase);
        } 
    }

    @Override
    public Suscripcion crearSuscripcion(Long organizacionId, SuscripcionRequest request) {
        Integer numMaestros = request.getNumMaestros();
        Integer numAlumnos =request.getNumAlumnos();
        Integer numMeses = request.getNumMeses();

        Usuario u = usuarioService.findCurrentUser();
        if (!(u instanceof Organizacion)) {
            throw new AccessDeniedException("Solo una organización puede crear una suscripción");
        }else{
            Organizacion organizacion = organizacionRepository.findById(organizacionId)
                .orElseThrow(() -> new IllegalArgumentException("La organización para la que intenta crear la suscripción no fue encontrada"));

            validarParametros(organizacion, numMaestros, numAlumnos, numMeses);

            if(!u.getId().equals(organizacionId)) {
                throw new AccessDeniedException("No se puede crear una suscripción para una organización diferente a la del usuario logueado");
            } else if (organizacion.getActivo()) {
                throw new IllegalArgumentException("La organización ya tiene una suscripción activa");
            } else{
    
                Double costoSuscripcion = calcularPrecioSuscripcion(numMaestros, numAlumnos, numMeses).doubleValue();

                Suscripcion nuevaSuscripcion = new Suscripcion(numMaestros, numAlumnos, costoSuscripcion, 
                                        java.time.LocalDate.now(), java.time.LocalDate.now().plusMonths(numMeses), organizacion);

                return suscripcionRepository.save(nuevaSuscripcion);

                // Añadir función para gestionar el pago de la suscripción (simulado por ahora)
            }
        }
    }

    private Double calcularPrecioSuscripcion(Integer numMaestros, Integer numAlumnos, Integer numMeses) {
        Double costoProfesores = numMaestros <= limitesBase.get("profesor") ? 
            numMaestros * preciosBase.get("profesor")
            : 
            (limitesBase.get("profesor") * preciosBase.get("profesor")) + 
            ((numMaestros - limitesBase.get("profesor")) * preciosBase.get("profesor_extra"));

        Double costoAlumnos = numAlumnos <= limitesBase.get("alumno") ? 
            numAlumnos * preciosBase.get("alumno") 
            : 
            (limitesBase.get("alumno") * preciosBase.get("alumno")) + 
            ((numAlumnos - limitesBase.get("alumno")) * preciosBase.get("alumno_extra"));

        return (costoProfesores + costoAlumnos) * numMeses;
    }

    private void validarParametros(Organizacion organizacion, Integer numMaestros, Integer numAlumnos, Integer numMeses) {
        if (numMaestros == null || numAlumnos == null || numMeses == null) {
            throw new IllegalArgumentException("Los parámetros de suscripción no pueden ser nulos");
        }

        if (numMaestros <= 0 || numAlumnos <= 0 || numMeses <= 0) {
            throw new IllegalArgumentException("El número de profesores, alumnos y meses debe ser mayor a cero");
        }

        Integer numMaestrosActuales = organizacion.getMaestros().size();
        Integer numAlumnosActuales = organizacion.getAlumnos().size();

        if (numMaestrosActuales > numMaestros) {
            throw new IllegalArgumentException(String.format(
                "No puedes contratar una suscripción para %d profesores porque la organización ya tiene %d registrados.", 
                numMaestros, numMaestrosActuales));
        }

        if (numAlumnosActuales > numAlumnos) {
            throw new IllegalArgumentException(String.format(
                "No puedes contratar una suscripción para %d alumnos porque la organización ya tiene %d registrados.", 
                numAlumnos, numAlumnosActuales));
        }
    }

}
