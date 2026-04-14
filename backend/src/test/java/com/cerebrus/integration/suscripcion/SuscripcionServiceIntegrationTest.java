package com.cerebrus.integration.suscripcion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;
import com.cerebrus.usuario.organizacion.Organizacion;
import com.cerebrus.usuario.organizacion.OrganizacionRepository;

@SpringBootTest(properties = {
    "GOOGLE_API_KEY_1=dummy-key-1",
    "GOOGLE_API_KEY_2=dummy-key-2",
    "GOOGLE_API_KEY_3=dummy-key-3",
    "GOOGLE_API_KEY_4=dummy-key-4",
    "GOOGLE_API_KEY_5=dummy-key-5"
})
class SuscripcionServiceIntegrationTest {

    @MockitoBean
    private SuscripcionRepository suscripcionRepository;

    @MockitoBean
    private OrganizacionRepository organizacionRepository;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private MockPagoService mockPagoService;

    @Autowired
    private SuscripcionServiceImpl service;

    private Organizacion organizacion;
    private Organizacion otraOrganizacion;
    private Maestro maestro;
    private Alumno alumno;

    @BeforeEach
    void setUp() {
        organizacion = crearOrganizacion(1L, 2, 3);
        otraOrganizacion = crearOrganizacion(2L, 1, 1);
        maestro = crearMaestro(10L);
        alumno = crearAlumno(11L);
    }

    @Test
    void obtenerSuscripcionesOrganizacion_comoOrganizacion_conSuscripciones_devuelveLista() {
        Suscripcion activa = crearSuscripcion(100L, organizacion, 5, 8, 99.0, EstadoPagoSuscripcion.PAGADA,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(20), "txn-100");
        organizacion.setSuscripciones(List.of(activa));

        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacion));

        List<SuscripcionDTO> resultado = service.obtenerSuscripcionesOrganizacion(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getId()).isEqualTo(100L);
        assertThat(resultado.getFirst().isActiva()).isTrue();
    }

    @Test
    void obtenerSuscripcionesOrganizacion_usuarioNoOrganizacion_lanzaAccessDenied() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        assertThatThrownBy(() -> service.obtenerSuscripcionesOrganizacion(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Solo una organización puede acceder a sus suscripciones");
    }

    @Test
    void obtenerSuscripcionesOrganizacion_organizacionDistinta_lanzaAccessDenied() {
        when(usuarioService.findCurrentUser()).thenReturn(otraOrganizacion);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacion));

        assertThatThrownBy(() -> service.obtenerSuscripcionesOrganizacion(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("diferente a la del usuario logueado");
    }

    @Test
    void obtenerSuscripcionesOrganizacion_sinSuscripciones_lanzaIllegalArgument() {
        organizacion.setSuscripciones(new ArrayList<>());

        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacion));

        assertThatThrownBy(() -> service.obtenerSuscripcionesOrganizacion(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La organización no tiene suscripciones");
    }

    @Test
    void obtenerSuscripcionOrganizacion_comoOrganizacion_devuelveDTO() {
        Suscripcion suscripcion = crearSuscripcion(101L, organizacion, 4, 6, 80.0, EstadoPagoSuscripcion.PAGADA,
                LocalDate.now().minusDays(2), LocalDate.now().plusDays(10), "txn-101");

        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(suscripcionRepository.findByIdAndOrganizacionId(101L, 1L)).thenReturn(Optional.of(suscripcion));

        SuscripcionDTO resultado = service.obtenerSuscripcionOrganizacion(1L, 101L);

        assertThat(resultado.getId()).isEqualTo(101L);
        assertThat(resultado.getNumMaestros()).isEqualTo(4);
        assertThat(resultado.isActiva()).isTrue();
    }

    @Test
    void obtenerSuscripcionOrganizacion_usuarioNoOrganizacion_lanzaAccessDenied() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> service.obtenerSuscripcionOrganizacion(1L, 101L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Solo una organización puede acceder a su suscripción");
    }

    @Test
    void obtenerSuscripcionOrganizacion_noEncontrada_lanzaIllegalArgument() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(suscripcionRepository.findByIdAndOrganizacionId(101L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerSuscripcionOrganizacion(1L, 101L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se encontró la suscripción de la organización");
    }

    @Test
    void obtenerSuscripcionOrganizacion_organizacionDiferente_lanzaAccessDenied() {
        Suscripcion suscripcion = crearSuscripcion(102L, otraOrganizacion, 4, 6, 80.0, EstadoPagoSuscripcion.PAGADA,
                LocalDate.now().minusDays(2), LocalDate.now().plusDays(10), "txn-102");

        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(suscripcionRepository.findByIdAndOrganizacionId(102L, 1L)).thenReturn(Optional.of(suscripcion));

        assertThatThrownBy(() -> service.obtenerSuscripcionOrganizacion(1L, 102L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("diferente a la del usuario logueado");
    }

    @Test
    void obtenerSuscripcionActivaOrganizacion_comoOrganizacion_devuelveUltimaActiva() {
        Suscripcion antigua = crearSuscripcion(103L, organizacion, 3, 5, 50.0, EstadoPagoSuscripcion.PAGADA,
                LocalDate.now().minusDays(10), LocalDate.now().plusDays(5), "txn-103");
        Suscripcion activa = crearSuscripcion(104L, organizacion, 4, 6, 60.0, EstadoPagoSuscripcion.PAGADA,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(20), "txn-104");
        organizacion.setSuscripciones(List.of(antigua, activa));

        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacion));

        SuscripcionDTO resultado = service.obtenerSuscripcionActivaOrganizacion(1L);

        assertThat(resultado.getId()).isEqualTo(104L);
        assertThat(resultado.isActiva()).isTrue();
    }

    @Test
    void obtenerSuscripcionActivaOrganizacion_usuarioNoOrganizacion_lanzaAccessDenied() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        assertThatThrownBy(() -> service.obtenerSuscripcionActivaOrganizacion(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Solo una organización puede acceder a su suscripción activa");
    }

    @Test
    void obtenerSuscripcionActivaOrganizacion_sinActiva_lanzaIllegalArgument() {
        Suscripcion inactiva = crearSuscripcion(105L, organizacion, 4, 6, 60.0, EstadoPagoSuscripcion.PENDIENTE,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(20), "txn-105");
        organizacion.setSuscripciones(List.of(inactiva));

        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacion));

        assertThatThrownBy(() -> service.obtenerSuscripcionActivaOrganizacion(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se encontró una suscripción activa");
    }

    @Test
    void obtenerPlanPrecios_comoOrganizacion_devuelvePlanes() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);

        PlanPreciosDTO resultado = service.obtenerPlanPrecios();

        assertThat(resultado.getLimitesBase()).containsEntry("profesor", 20);
        assertThat(resultado.getLimitesBase()).containsEntry("alumno", 50);
        assertThat(resultado.getPreciosBase()).containsEntry("profesor", 10.0);
        assertThat(resultado.getPreciosBase()).containsEntry("alumno_extra", 3.0);
    }

    @Test
    void obtenerPlanPrecios_usuarioNoOrganizacion_lanzaAccessDenied() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        assertThatThrownBy(() -> service.obtenerPlanPrecios())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Solo una organización puede acceder a los planes de precios");
    }

    @Test
    void resumirSuscripcionAComprar_conValoresBasicos_devuelveResumen() {
        SuscripcionRequest request = new SuscripcionRequest(2, 3, 4);

        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacion));

        ResumenCompraDTO resultado = service.resumirSuscripcionAComprar(1L, request);

        assertThat(resultado.getNombreUsuario()).isEqualTo(organizacion.getNombreUsuario());
        assertThat(resultado.getNumMaestros()).isEqualTo(2);
        assertThat(resultado.getNumAlumnos()).isEqualTo(3);
        assertThat(resultado.getMeses()).isEqualTo(4);
        assertThat(resultado.getPrecioTotal()).isEqualTo(140.0);
        assertThat(resultado.getFechaInicio()).isEqualTo(LocalDate.now());
        assertThat(resultado.getFechaFin()).isEqualTo(LocalDate.now().plusMonths(4));
    }

    @Test
    void resumirSuscripcionAComprar_conValoresSuperiores_devuelveResumenConExtras() {
        SuscripcionRequest request = new SuscripcionRequest(21, 51, 2);

        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacion));

        ResumenCompraDTO resultado = service.resumirSuscripcionAComprar(1L, request);

        assertThat(resultado.getPrecioTotal()).isEqualTo(920.0);
    }

    @Test
    void resumirSuscripcionAComprar_usuarioNoOrganizacion_lanzaAccessDenied() {
        when(usuarioService.findCurrentUser()).thenReturn(alumno);

        assertThatThrownBy(() -> service.resumirSuscripcionAComprar(1L, new SuscripcionRequest(2, 3, 4)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Solo una organización puede crear una suscripción");
    }

    @Test
    void resumirSuscripcionAComprar_organizacionDiferente_lanzaAccessDenied() {
        when(usuarioService.findCurrentUser()).thenReturn(otraOrganizacion);

        assertThatThrownBy(() -> service.resumirSuscripcionAComprar(1L, new SuscripcionRequest(2, 3, 4)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("diferente a la del usuario logueado");
    }

    @Test
    void resumirSuscripcionAComprar_organizacionNoEncontrada_lanzaIllegalArgument() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resumirSuscripcionAComprar(1L, new SuscripcionRequest(2, 3, 4)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Organización no encontrada");
    }

    @Test
    void resumirSuscripcionAComprar_parametrosNulos_lanzaIllegalArgument() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacion));

        assertThatThrownBy(() -> service.resumirSuscripcionAComprar(1L, new SuscripcionRequest(null, 3, 4)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pueden ser nulos");
    }

    @Test
    void resumirSuscripcionAComprar_parametrosInvalidos_lanzaIllegalArgument() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacion));

        assertThatThrownBy(() -> service.resumirSuscripcionAComprar(1L, new SuscripcionRequest(0, 3, 4)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("debe ser mayor a cero");
    }

    @Test
    void resumirSuscripcionAComprar_menosMaestrosQueActuales_lanzaIllegalArgument() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacion));

        assertThatThrownBy(() -> service.resumirSuscripcionAComprar(1L, new SuscripcionRequest(1, 3, 4)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("porque la organización ya tiene 2 registrados");
    }

    @Test
    void resumirSuscripcionAComprar_menosAlumnosQueActuales_lanzaIllegalArgument() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacion));

        assertThatThrownBy(() -> service.resumirSuscripcionAComprar(1L, new SuscripcionRequest(2, 2, 4)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("porque la organización ya tiene 3 registrados");
    }

    @Test
    void crearSuscripcion_conDatosValidos_creaPagoPendiente() {
        SuscripcionRequest request = new SuscripcionRequest(21, 51, 2);
        SesionPagoDTO sesionPagoDTO = new SesionPagoDTO("txn-200", "https://pago.test/txn-200");

        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(suscripcionRepository.findByOrganizacionIdSuscripcionActiva(1L)).thenReturn(Optional.empty());
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacion));
        when(suscripcionRepository.save(any(Suscripcion.class))).thenAnswer(invocation -> {
            Suscripcion suscripcion = invocation.getArgument(0);
            if (suscripcion.getId() == null) {
                suscripcion.setId(200L);
            }
            return suscripcion;
        });
        when(mockPagoService.crearSesionDePago(200L, 920.0)).thenReturn(sesionPagoDTO);

        PagoResponseDTO resultado = service.crearSuscripcion(1L, request);

        assertThat(resultado.getTransaccionId()).isEqualTo("txn-200");
        assertThat(resultado.getUrlPago()).isEqualTo("https://pago.test/txn-200");
        verify(mockPagoService).crearSesionDePago(200L, 920.0);
    }

    @Test
    void crearSuscripcion_usuarioNoOrganizacion_lanzaAccessDenied() {
        when(usuarioService.findCurrentUser()).thenReturn(maestro);

        assertThatThrownBy(() -> service.crearSuscripcion(1L, new SuscripcionRequest(2, 3, 4)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Solo una organización puede realizar una suscripción");
    }

    @Test
    void crearSuscripcion_organizacionDiferente_lanzaAccessDenied() {
        when(usuarioService.findCurrentUser()).thenReturn(otraOrganizacion);

        assertThatThrownBy(() -> service.crearSuscripcion(1L, new SuscripcionRequest(2, 3, 4)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("diferente a la del usuario logueado");
    }

    @Test
    void crearSuscripcion_yaTieneActiva_lanzaIllegalArgument() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(suscripcionRepository.findByOrganizacionIdSuscripcionActiva(1L))
                .thenReturn(Optional.of(crearSuscripcion(300L, organizacion, 2, 3, 100.0, EstadoPagoSuscripcion.PAGADA,
                        LocalDate.now().minusDays(1), LocalDate.now().plusDays(5), "txn-300")));

        assertThatThrownBy(() -> service.crearSuscripcion(1L, new SuscripcionRequest(2, 3, 4)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya tiene una suscripción activa");
    }

    @Test
    void crearSuscripcion_organizacionNoEncontrada_lanzaIllegalArgument() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(suscripcionRepository.findByOrganizacionIdSuscripcionActiva(1L)).thenReturn(Optional.empty());
        when(organizacionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearSuscripcion(1L, new SuscripcionRequest(2, 3, 4)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Organización no encontrada");
    }

    @Test
    void crearSuscripcion_parametrosNulos_lanzaIllegalArgument() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(suscripcionRepository.findByOrganizacionIdSuscripcionActiva(1L)).thenReturn(Optional.empty());
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacion));

        assertThatThrownBy(() -> service.crearSuscripcion(1L, new SuscripcionRequest(null, 3, 4)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pueden ser nulos");
    }

    @Test
    void crearSuscripcion_parametrosInvalidos_lanzaIllegalArgument() {
        when(usuarioService.findCurrentUser()).thenReturn(organizacion);
        when(suscripcionRepository.findByOrganizacionIdSuscripcionActiva(1L)).thenReturn(Optional.empty());
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(organizacion));

        assertThatThrownBy(() -> service.crearSuscripcion(1L, new SuscripcionRequest(2, 0, 4)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("debe ser mayor a cero");
    }

    @Test
    void confirmarPagoExitoso_conTransaccionPendiente_confirmaPago() {
        Suscripcion pendiente = crearSuscripcion(400L, organizacion, 2, 3, 120.0, EstadoPagoSuscripcion.PENDIENTE,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30), "txn-400");

        when(suscripcionRepository.findByTransaccionId("txn-400")).thenReturn(Optional.of(pendiente));

        service.confirmarPagoExitoso("txn-400");

        assertThat(pendiente.getEstadoPagoSuscripcion()).isEqualTo(EstadoPagoSuscripcion.PAGADA);
        verify(suscripcionRepository).save(pendiente);
    }

    @Test
    void confirmarPagoExitoso_transaccionNoEncontrada_lanzaIllegalArgument() {
        when(suscripcionRepository.findByTransaccionId("txn-missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmarPagoExitoso("txn-missing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transacción no encontrada");
    }

    @Test
    void confirmarPagoExitoso_yaPagada_lanzaIllegalArgument() {
        Suscripcion pagada = crearSuscripcion(401L, organizacion, 2, 3, 120.0, EstadoPagoSuscripcion.PAGADA,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30), "txn-401");

        when(suscripcionRepository.findByTransaccionId("txn-401")).thenReturn(Optional.of(pagada));

        assertThatThrownBy(() -> service.confirmarPagoExitoso("txn-401"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya ha sido pagada");
    }

    private static Organizacion crearOrganizacion(Long id, int maestrosActuales, int alumnosActuales) {
        Organizacion organizacion = new Organizacion();
        organizacion.setId(id);
        organizacion.setNombre("Organizacion Test");
        organizacion.setPrimerApellido("Centro");
        organizacion.setSegundoApellido("Uno");
        organizacion.setNombreUsuario("org" + id);
        organizacion.setCorreoElectronico("org" + id + "@cerebrus.test");
        organizacion.setContrasena("secret");
        organizacion.setNombreCentro("Centro Test " + id);
        organizacion.setMaestros(new ArrayList<>());
        organizacion.setAlumnos(new ArrayList<>());
        organizacion.setSuscripciones(new ArrayList<>());

        for (int i = 0; i < maestrosActuales; i++) {
            organizacion.getMaestros().add(crearMaestro((long) (100 + i)));
        }
        for (int i = 0; i < alumnosActuales; i++) {
            organizacion.getAlumnos().add(crearAlumno((long) (200 + i)));
        }

        return organizacion;
    }

    private static Maestro crearMaestro(Long id) {
        Maestro maestro = new Maestro();
        maestro.setId(id);
        maestro.setNombre("Maestro " + id);
        maestro.setPrimerApellido("Apellido");
        maestro.setSegundoApellido("Segundo");
        maestro.setNombreUsuario("maestro" + id);
        maestro.setCorreoElectronico("maestro" + id + "@cerebrus.test");
        maestro.setContrasena("secret");
        return maestro;
    }

    private static Alumno crearAlumno(Long id) {
        Alumno alumno = new Alumno();
        alumno.setId(id);
        alumno.setNombre("Alumno " + id);
        alumno.setPrimerApellido("Apellido");
        alumno.setSegundoApellido("Segundo");
        alumno.setNombreUsuario("alumno" + id);
        alumno.setCorreoElectronico("alumno" + id + "@cerebrus.test");
        alumno.setContrasena("secret");
        alumno.setPuntos(0);
        return alumno;
    }

    private static Suscripcion crearSuscripcion(Long id, Organizacion org, int numMaestros, int numAlumnos,
            Double precio, EstadoPagoSuscripcion estado, LocalDate fechaInicio, LocalDate fechaFin, String transaccionId) {
        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setId(id);
        suscripcion.setOrganizacion(org);
        suscripcion.setNumMaestros(numMaestros);
        suscripcion.setNumAlumnos(numAlumnos);
        suscripcion.setPrecio(precio);
        suscripcion.setEstadoPagoSuscripcion(estado);
        suscripcion.setFechaInicio(fechaInicio);
        suscripcion.setFechaFin(fechaFin);
        suscripcion.setTransaccionId(transaccionId);
        return suscripcion;
    }
}