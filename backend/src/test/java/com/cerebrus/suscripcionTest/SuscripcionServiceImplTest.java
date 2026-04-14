package com.cerebrus.suscripcionTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.cerebrus.comun.enumerados.EstadoPagoSuscripcion;
import com.cerebrus.suscripcion.Suscripcion;
import com.cerebrus.suscripcion.SuscripcionRepository;
import com.cerebrus.suscripcion.SuscripcionServiceImpl;
import com.cerebrus.suscripcion.dto.PlanPreciosDTO;
import com.cerebrus.suscripcion.dto.SuscripcionDTO;
import com.cerebrus.suscripcion.dto.SuscripcionRequest;
import com.cerebrus.suscripcion.pago.MockPagoService;
import com.cerebrus.suscripcion.pago.dto.PagoResponseDTO;
import com.cerebrus.suscripcion.pago.dto.ResumenCompraDTO;
import com.cerebrus.suscripcion.pago.dto.SesionPagoDTO;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;
import com.cerebrus.usuario.organizacion.Organizacion;
import com.cerebrus.usuario.organizacion.OrganizacionRepository;

@ExtendWith(MockitoExtension.class)
class SuscripcionServiceImplTest {

    @Mock
    private SuscripcionRepository suscripcionRepository;

    @Mock
    private OrganizacionRepository organizacionRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private MockPagoService mockPagoService;

    @InjectMocks
    private SuscripcionServiceImpl service;

    private Organizacion organizacion;
    private Suscripcion suscripcionActiva;
    private Suscripcion suscripcionInactiva;

    @BeforeEach
    void setUp() {
        organizacion = crearOrganizacion(1001L, 2, 3);
        suscripcionActiva = crearSuscripcion(3001L, organizacion, EstadoPagoSuscripcion.PAGADA,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        suscripcionInactiva = crearSuscripcion(3002L, organizacion, EstadoPagoSuscripcion.PENDIENTE,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
    }

    @Test
    void obtenerSuscripcionesOrganizacion_devuelveListaCuandoTodoEsCorrecto() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(organizacionRepository.findById(1001L)).thenReturn(Optional.of(organizacion));

        organizacion.setSuscripciones(new ArrayList<>(List.of(suscripcionActiva, suscripcionInactiva)));

        List<SuscripcionDTO> resultado = service.obtenerSuscripcionesOrganizacion(1001L);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getId()).isEqualTo(3001L);
        assertThat(resultado.get(0).isActiva()).isTrue();
        assertThat(resultado.get(1).getId()).isEqualTo(3002L);
        assertThat(resultado.get(1).isActiva()).isFalse();
    }

    @Test
    void obtenerSuscripcionesOrganizacion_lanzaAccessDenied_siNoEsOrganizacion() {
        Usuario usuario = new Usuario() {};
        usuario.setId(9L);
        when(usuarioService.findCurrentUser()).thenReturn(usuario);

        assertThatThrownBy(() -> service.obtenerSuscripcionesOrganizacion(1001L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo una organización puede acceder a sus suscripciones");
    }

    @Test
    void obtenerSuscripcionesOrganizacion_lanzaAccessDenied_siOrganizacionDiferente() {
        Organizacion otra = crearOrganizacion(2002L, 1, 1);
        when(usuarioService.findCurrentUser()).thenReturn(otra);
        when(organizacionRepository.findById(1001L)).thenReturn(Optional.of(organizacion));

        assertThatThrownBy(() -> service.obtenerSuscripcionesOrganizacion(1001L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("diferente a la del usuario logueado");
    }

    @Test
    void obtenerSuscripcionesOrganizacion_lanzaIllegalArgument_siNoHaySuscripciones() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(organizacionRepository.findById(1001L)).thenReturn(Optional.of(organizacion));
        organizacion.setSuscripciones(new ArrayList<>());

        assertThatThrownBy(() -> service.obtenerSuscripcionesOrganizacion(1001L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no tiene suscripciones");
    }

    @Test
    void obtenerSuscripcionOrganizacion_devuelveDTOCuandoExisteYPerteneceALaOrg() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(suscripcionRepository.findByIdAndOrganizacionId(3001L, 1001L)).thenReturn(Optional.of(suscripcionActiva));

        SuscripcionDTO resultado = service.obtenerSuscripcionOrganizacion(1001L, 3001L);

        assertThat(resultado.getId()).isEqualTo(3001L);
        assertThat(resultado.isActiva()).isTrue();
        assertThat(resultado.getEstadoPago()).isEqualTo(EstadoPagoSuscripcion.PAGADA);
    }

    @Test
    void obtenerSuscripcionOrganizacion_lanzaIllegalArgument_siNoExiste() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(suscripcionRepository.findByIdAndOrganizacionId(999L, 1001L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerSuscripcionOrganizacion(1001L, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se encontró la suscripción de la organización");
    }

    @Test
    void obtenerSuscripcionOrganizacion_lanzaAccessDenied_siLaSuscripcionNoEsDeLaOrg() {
        Organizacion otra = crearOrganizacion(2002L, 1, 1);
        Suscripcion suscripcionOtra = crearSuscripcion(888L, otra, EstadoPagoSuscripcion.PAGADA,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(10));
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(suscripcionRepository.findByIdAndOrganizacionId(888L, 1001L)).thenReturn(Optional.of(suscripcionOtra));

        assertThatThrownBy(() -> service.obtenerSuscripcionOrganizacion(1001L, 888L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("diferente a la del usuario logueado");
    }

    @Test
    void obtenerSuscripcionActivaOrganizacion_devuelveUltimaSuscripcionActiva() {
        Suscripcion antigua = crearSuscripcion(3000L, organizacion, EstadoPagoSuscripcion.PAGADA,
                LocalDate.now().minusDays(20), LocalDate.now().minusDays(10));
        organizacion.setSuscripciones(new ArrayList<>(List.of(antigua, suscripcionActiva)));
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(organizacionRepository.findById(1001L)).thenReturn(Optional.of(organizacion));

        SuscripcionDTO resultado = service.obtenerSuscripcionActivaOrganizacion(1001L);

        assertThat(resultado.getId()).isEqualTo(3001L);
        assertThat(resultado.isActiva()).isTrue();
    }

    @Test
    void obtenerSuscripcionActivaOrganizacion_lanzaAccessDenied_siNoEsOrganizacion() {
        Usuario usuario = new Usuario() {};
        usuario.setId(9L);
        when(usuarioService.findCurrentUser()).thenReturn(usuario);

        assertThatThrownBy(() -> service.obtenerSuscripcionActivaOrganizacion(1001L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo una organización puede acceder a su suscripción activa");
    }

    @Test
    void obtenerSuscripcionActivaOrganizacion_lanzaIllegalArgument_siNoEstaActiva() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(organizacionRepository.findById(1001L)).thenReturn(Optional.of(organizacion));
        organizacion.setSuscripciones(new ArrayList<>(List.of(suscripcionInactiva)));

        assertThatThrownBy(() -> service.obtenerSuscripcionActivaOrganizacion(1001L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se encontró una suscripción activa");
    }

    @Test
    void obtenerPlanPrecios_devuelveMapasEsperados() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);

        PlanPreciosDTO resultado = service.obtenerPlanPrecios();

        assertThat(resultado.getLimitesBase()).containsEntry("profesor", 20).containsEntry("alumno", 50);
        assertThat(resultado.getPreciosBase()).containsEntry("profesor", 10.0).containsEntry("alumno_extra", 3.0);
    }

    @Test
    void obtenerPlanPrecios_lanzaAccessDenied_siNoEsOrganizacion() {
        Usuario usuario = new Usuario() {};
        usuario.setId(11L);
        when(usuarioService.findCurrentUser()).thenReturn(usuario);

        assertThatThrownBy(() -> service.obtenerPlanPrecios())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo una organización puede acceder a los planes de precios");
    }

    @Test
    void resumirSuscripcionAComprar_devuelveResumenYCalculaPrecioCorrectamente() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(organizacionRepository.findById(1001L)).thenReturn(Optional.of(organizacion));

        SuscripcionRequest request = crearRequest(2, 3, 2);

        ResumenCompraDTO resultado = service.resumirSuscripcionAComprar(1001L, request);

        assertThat(resultado.getNombreUsuario()).isEqualTo("org_admin_1001");
        assertThat(resultado.getNombreCentro()).isEqualTo("Instituto Cerebrus");
        assertThat(resultado.getNumMaestros()).isEqualTo(2);
        assertThat(resultado.getNumAlumnos()).isEqualTo(3);
        assertThat(resultado.getMeses()).isEqualTo(2);
        assertThat(resultado.getFechaInicio()).isEqualTo(LocalDate.now());
        assertThat(resultado.getFechaFin()).isEqualTo(LocalDate.now().plusMonths(2));
        assertThat(resultado.getPrecioTotal()).isEqualTo(70.0);
    }

    @Test
    void resumirSuscripcionAComprar_lanzaAccessDenied_siNoEsOrganizacion() {
        Usuario usuario = new Usuario() {};
        usuario.setId(11L);
        when(usuarioService.findCurrentUser()).thenReturn(usuario);

        assertThatThrownBy(() -> service.resumirSuscripcionAComprar(1001L, crearRequest(1, 1, 1)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo una organización puede crear una suscripción");
    }

    @Test
    void resumirSuscripcionAComprar_lanzaAccessDenied_siOrganizacionDiferente() {
        Organizacion otra = crearOrganizacion(2002L, 1, 1);
        when(usuarioService.findCurrentUser()).thenReturn(otra);

        assertThatThrownBy(() -> service.resumirSuscripcionAComprar(1001L, crearRequest(1, 1, 1)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("diferente a la del usuario logueado");
    }

    @Test
    void resumirSuscripcionAComprar_lanzaIllegalArgument_siParametrosSonNulosOCero() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(organizacionRepository.findById(1001L)).thenReturn(Optional.of(organizacion));

        assertThatThrownBy(() -> service.resumirSuscripcionAComprar(1001L, crearRequest(null, 1, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pueden ser nulos");

        assertThatThrownBy(() -> service.resumirSuscripcionAComprar(1001L, crearRequest(0, 1, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("debe ser mayor a cero");
    }

    @Test
    void resumirSuscripcionAComprar_lanzaIllegalArgument_siLimitesActualesSuperanSolicitados() {
        Organizacion orgConMasMaestros = crearOrganizacion(1001L, 3, 3);
        when(usuarioService.findCurrentUser()).thenReturn(orgConMasMaestros);
        when(organizacionRepository.findById(1001L)).thenReturn(Optional.of(orgConMasMaestros));

        assertThatThrownBy(() -> service.resumirSuscripcionAComprar(1001L, crearRequest(2, 5, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("profesores");

        Organizacion orgConMasAlumnos = crearOrganizacion(1001L, 1, 5);
        when(usuarioService.findCurrentUser()).thenReturn(orgConMasAlumnos);
        when(organizacionRepository.findById(1001L)).thenReturn(Optional.of(orgConMasAlumnos));

        assertThatThrownBy(() -> service.resumirSuscripcionAComprar(1001L, crearRequest(5, 2, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("alumnos");
    }

    @Test
    void crearSuscripcion_creaPendienteYGeneraSesionDePago() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(suscripcionRepository.findByOrganizacionIdSuscripcionActiva(1001L)).thenReturn(Optional.empty());
        when(organizacionRepository.findById(1001L)).thenReturn(Optional.of(organizacion));
        when(suscripcionRepository.save(any(Suscripcion.class))).thenAnswer(invocation -> {
            Suscripcion suscripcion = invocation.getArgument(0);
            if (suscripcion.getId() == null) {
                suscripcion.setId(77L);
            }
            return suscripcion;
        });
        when(mockPagoService.crearSesionDePago(77L, 70.0))
                .thenReturn(new SesionPagoDTO("txn-123", "http://front/simulador-pago?txn=txn-123&susId=77"));

        PagoResponseDTO resultado = service.crearSuscripcion(1001L, crearRequest(2, 3, 2));

        assertThat(resultado.getTransaccionId()).isEqualTo("txn-123");
        assertThat(resultado.getUrlPago()).contains("/simulador-pago");
        verify(suscripcionRepository, times(2)).save(any(Suscripcion.class));
        verify(mockPagoService).crearSesionDePago(77L, 70.0);
    }

    @Test
    void crearSuscripcion_lanzaIllegalArgument_siYaTieneActiva() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(suscripcionRepository.findByOrganizacionIdSuscripcionActiva(1001L)).thenReturn(Optional.of(suscripcionActiva));

        assertThatThrownBy(() -> service.crearSuscripcion(1001L, crearRequest(2, 3, 2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La organización ya tiene una suscripción activa.");

        verify(mockPagoService, never()).crearSesionDePago(any(), any());
    }

    @Test
    void crearSuscripcion_lanzaAccessDenied_siNoEsOrganizacion() {
        Usuario usuario = new Usuario() {};
        usuario.setId(11L);
        when(usuarioService.findCurrentUser()).thenReturn(usuario);

        assertThatThrownBy(() -> service.crearSuscripcion(1001L, crearRequest(2, 3, 2)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Solo una organización puede realizar una suscripción");
    }

    @Test
    void crearSuscripcion_lanzaAccessDenied_siOrganizacionDiferente() {
        Organizacion otra = crearOrganizacion(2002L, 1, 1);
        when(usuarioService.findCurrentUser()).thenReturn(otra);

        assertThatThrownBy(() -> service.crearSuscripcion(1001L, crearRequest(2, 3, 2)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("diferente a la del usuario logueado");
    }

    @Test
    void confirmarPagoExitoso_cambiaEstadoAPagada() {
        Suscripcion pendiente = crearSuscripcion(3009L, organizacion, EstadoPagoSuscripcion.PENDIENTE,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        pendiente.setTransaccionId("txn-999");
        when(suscripcionRepository.findByTransaccionId("txn-999")).thenReturn(Optional.of(pendiente));

        service.confirmarPagoExitoso("txn-999");

        assertThat(pendiente.getEstadoPagoSuscripcion()).isEqualTo(EstadoPagoSuscripcion.PAGADA);
        verify(suscripcionRepository).save(pendiente);
    }

    @Test
    void confirmarPagoExitoso_lanzaIllegalArgument_siNoExisteTransaccion() {
        when(suscripcionRepository.findByTransaccionId("txn-x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmarPagoExitoso("txn-x"))
                .isInstanceOf(IllegalArgumentException.class)
.hasMessageContaining("Transacción no encontrada");    }

    @Test
    void confirmarPagoExitoso_lanzaIllegalArgument_siYaEstaPagada() {
        Suscripcion pagada = crearSuscripcion(3010L, organizacion, EstadoPagoSuscripcion.PAGADA,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        pagada.setTransaccionId("txn-pagada");
        when(suscripcionRepository.findByTransaccionId("txn-pagada")).thenReturn(Optional.of(pagada));

        assertThatThrownBy(() -> service.confirmarPagoExitoso("txn-pagada"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Esta suscripción ya ha sido pagada");
    }

    private static Organizacion crearOrganizacion(Long id, int maestros, int alumnos) {
        Organizacion organizacion = new Organizacion();
        organizacion.setId(id);
        organizacion.setNombreCentro("Instituto Cerebrus");
        organizacion.setNombre("Isa");
        organizacion.setPrimerApellido("Sanchez");
        organizacion.setSegundoApellido("Jac");
        organizacion.setNombreUsuario("org_admin_" + id);
        organizacion.setCorreoElectronico("isasanchezjac@gmail.com");
        organizacion.setMaestros(crearMaestros(maestros, organizacion));
        organizacion.setAlumnos(crearAlumnos(alumnos, organizacion));
        organizacion.setSuscripciones(new ArrayList<>());
        return organizacion;
    }

    private static List<Maestro> crearMaestros(int cantidad, Organizacion organizacion) {
        List<Maestro> maestros = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            Maestro maestro = new Maestro();
            maestro.setId(100L + i);
            maestro.setOrganizacion(organizacion);
            maestros.add(maestro);
        }
        return maestros;
    }

    private static List<Alumno> crearAlumnos(int cantidad, Organizacion organizacion) {
        List<Alumno> alumnos = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            Alumno alumno = new Alumno();
            alumno.setId(200L + i);
            alumno.setOrganizacion(organizacion);
            alumnos.add(alumno);
        }
        return alumnos;
    }

    private static Suscripcion crearSuscripcion(Long id, Organizacion organizacion, EstadoPagoSuscripcion estado,
            LocalDate inicio, LocalDate fin) {
        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setId(id);
        suscripcion.setOrganizacion(organizacion);
        suscripcion.setNumMaestros(10);
        suscripcion.setNumAlumnos(20);
        suscripcion.setPrecio(99.99);
        suscripcion.setFechaInicio(inicio);
        suscripcion.setFechaFin(fin);
        suscripcion.setEstadoPagoSuscripcion(estado);
        return suscripcion;
    }

    private static SuscripcionRequest crearRequest(Integer numMaestros, Integer numAlumnos, Integer numMeses) {
        return new SuscripcionRequest(numMaestros, numAlumnos, numMeses);
    }
}
