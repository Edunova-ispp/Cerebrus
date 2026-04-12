package com.cerebrus.usuario.organizacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cerebrus.exceptions.ResourceNotFoundException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioRepository;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.alumno.Alumno;
import com.cerebrus.usuario.maestro.Maestro;
import com.cerebrus.usuario.organizacion.dto.UsuarioActualizarDTO;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;

import com.cerebrus.usuario.organizacion.dto.CreateUserRequest;

@Service
@Transactional
public class OrganizacionServiceImpl implements OrganizacionService {


    private final OrganizacionRepository organizacionRepository;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public OrganizacionServiceImpl(OrganizacionRepository organizacionRepository, PasswordEncoder passwordEncoder, UsuarioService usuarioService, UsuarioRepository usuarioRepository) {
        this.organizacionRepository = organizacionRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Maestro> listarMaestros(Long organizacionId, int page, int size) {
        Organizacion organizacion = validarYObtenerOrganizacionPropietaria(organizacionId, "listar maestros");
        // Comprobar suscripciones activas antes de listar maestros
        boolean tieneSuscripcionActiva = organizacion.getActivo() != null && organizacion.getActivo();
        if (!tieneSuscripcionActiva) {
            throw new AccessDeniedException("La organización no tiene una suscripción activa. No puede listar maestros.");
        }
        // Forzar carga de maestros dentro de la transación
        Hibernate.initialize(organizacion.getMaestros());
        // Devolver copias planas para evitar serialización de relaciones lazy
        List<Maestro> maestros = organizacion.getMaestros().stream()
                .map(this::toSafeMaestro)
                .toList();
        return paginarLista(maestros, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Alumno> listarAlumnos(Long organizacionId, int page, int size) {
        Organizacion organizacion = validarYObtenerOrganizacionPropietaria(organizacionId, "listar alumnos");
        // Comprobar suscripciones activas antes de listar alumnos
        boolean tieneSuscripcionActiva = organizacion.getActivo() != null && organizacion.getActivo();
        if (!tieneSuscripcionActiva) {
            throw new AccessDeniedException("La organización no tiene una suscripción activa. No puede listar alumnos.");
        }
        // Forzar carga de alumnos dentro de la transación
        Hibernate.initialize(organizacion.getAlumnos());
        // Devolver copias planas para evitar serialización de relaciones lazy
        List<Alumno> alumnos = organizacion.getAlumnos().stream()
                .map(this::toSafeAlumno)
                .toList();
        return paginarLista(alumnos, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarUsuario(Long organizacionId, Long usuarioId) {
        Organizacion organizacion = validarYObtenerOrganizacionPropietaria(organizacionId, "buscar usuarios");
        // Comprobar suscripciones activas antes de buscar usuarios
        boolean tieneSuscripcionActiva = organizacion.getActivo() != null && organizacion.getActivo();
        if (!tieneSuscripcionActiva) {
            throw new AccessDeniedException("La organización no tiene una suscripción activa. No puede buscar usuarios.");
        }
        // Forzar carga de ambas colecciones dentro de la transacción
        Hibernate.initialize(organizacion.getMaestros());
        Hibernate.initialize(organizacion.getAlumnos());
        // Hacer copias de las colecciones para evitar lazy loading posterior
        List<Usuario> usuarios = Stream.concat(
                        new ArrayList<>(organizacion.getMaestros()).stream().map(m -> (Usuario) m),
                        new ArrayList<>(organizacion.getAlumnos()).stream().map(a -> (Usuario) a))
                .toList();
        Usuario usuario = usuarios.stream()
                .filter(u -> u.getId().equals(usuarioId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));
        return toSafeUsuario(usuario);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long organizacionId, Long usuarioId) {
        Organizacion organizacion = validarYObtenerOrganizacionPropietaria(organizacionId, "eliminar usuarios");
        Hibernate.initialize(organizacion.getMaestros());
        Hibernate.initialize(organizacion.getAlumnos());
        // Buscar en la colección gestionada por Hibernate directamente
        Maestro maestro = organizacion.getMaestros().stream()
                .filter(m -> m.getId().equals(usuarioId))
                .findFirst().orElse(null);
        if (maestro != null) {
            organizacion.getMaestros().remove(maestro);
            organizacionRepository.save(organizacion);
            return;
        }
        Alumno alumno = organizacion.getAlumnos().stream()
                .filter(a -> a.getId().equals(usuarioId))
                .findFirst().orElse(null);
        if (alumno != null) {
            organizacion.getAlumnos().remove(alumno);
            organizacionRepository.save(organizacion);
            return;
        }
        throw new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId);
    }

    @Override
    @Transactional
    public Usuario actualizarUsuario(Long organizacionId, Long usuarioId, UsuarioActualizarDTO usuarioActualizado) {
        validarYObtenerOrganizacionPropietaria(organizacionId, "actualizar usuarios");
        // Buscar la entidad gestionada directamente desde el repositorio
        Usuario usuarioExistente = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));
        validarNombreUsuarioUnicoEnOrganizacion(
                organizacionRepository.findById(organizacionId).orElseThrow(),
                usuarioExistente.getId(), usuarioActualizado.getNombreUsuario());
        // Solo se permiten actualizar ciertos campos
        if (usuarioActualizado.getNombre() != null) usuarioExistente.setNombre(usuarioActualizado.getNombre());
        if (usuarioActualizado.getPrimerApellido() != null) usuarioExistente.setPrimerApellido(usuarioActualizado.getPrimerApellido());
        usuarioExistente.setSegundoApellido(usuarioActualizado.getSegundoApellido());
        if (usuarioActualizado.getNombreUsuario() != null) usuarioExistente.setNombreUsuario(usuarioActualizado.getNombreUsuario());
        if (usuarioActualizado.getCorreoElectronico() != null) usuarioExistente.setCorreoElectronico(usuarioActualizado.getCorreoElectronico());
        if (usuarioActualizado.getContrasena() != null && !usuarioActualizado.getContrasena().isBlank()) {
            usuarioExistente.setContrasena(passwordEncoder.encode(usuarioActualizado.getContrasena()));
        }
        return toSafeUsuario(usuarioRepository.save(usuarioExistente));
    }

    private Organizacion validarYObtenerOrganizacionPropietaria(Long organizacionId, String accion) {
        Usuario u = usuarioService.findCurrentUser();
        if (!u.getId().equals(organizacionId)) {
            throw new IllegalArgumentException("Solo la organización propietaria puede " + accion);
        }
        Organizacion org = organizacionRepository.findById(organizacionId)
            .orElseThrow(() -> new ResourceNotFoundException("Organización no encontrada con ID: " + organizacionId));
        // Forzar carga de suscripciones para evitar lazy loading al serializar
        Hibernate.initialize(org.getSuscripciones());
        return org;
    }

    private <T> Page<T> paginarLista(List<T> elementos, int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("El parámetro 'page' no puede ser negativo");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("El parámetro 'size' debe ser mayor que 0");
        }
        Pageable pageable = PageRequest.of(page, size);
        int inicio = (int) pageable.getOffset();
        if (inicio >= elementos.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, elementos.size());
        }
        int fin = Math.min(inicio + pageable.getPageSize(), elementos.size());
        return new PageImpl<>(elementos.subList(inicio, fin), pageable, elementos.size());
    }

    private void validarNombreUsuarioUnicoEnOrganizacion(Organizacion organizacion, Long usuarioIdActual, String nombreUsuarioNuevo) {
        if (nombreUsuarioNuevo == null || nombreUsuarioNuevo.isBlank()) {
            return;
        }
        Hibernate.initialize(organizacion.getMaestros());
        // Devolver copias planas para evitar serialización de relaciones lazy
        List<Maestro> maestros = organizacion.getMaestros().stream()
                .map(this::toSafeMaestro)
                .toList();
        boolean existeEnMaestros = maestros.stream()
                .filter(m -> !m.getId().equals(usuarioIdActual)).anyMatch(m -> m.getNombreUsuario().equals(nombreUsuarioNuevo));
        Hibernate.initialize(organizacion.getAlumnos());
        // Devolver copias planas para evitar serialización de relaciones lazy
        List<Alumno> alumnos = organizacion.getAlumnos().stream()
                .map(this::toSafeAlumno)
                .toList();
        boolean existeEnAlumnos = alumnos.stream()
                .filter(a -> !a.getId().equals(usuarioIdActual)).anyMatch(a -> a.getNombreUsuario().equals(nombreUsuarioNuevo));
        if (existeEnMaestros || existeEnAlumnos) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso");
        }
    }

    private Usuario toSafeUsuario(Usuario usuario) {
        if (usuario instanceof Maestro maestro) {
            return toSafeMaestro(maestro);
        }
        if (usuario instanceof Alumno alumno) {
            return toSafeAlumno(alumno);
        }
        return usuario;
    }

    @Override
    public void crearUsuario(CreateUserRequest request) {
        Usuario usuarioActual = usuarioService.findCurrentUser();
        
        if (!(usuarioActual instanceof Organizacion)) {
            throw new AccessDeniedException("Solo usuarios con rol ORGANIZACIÓN pueden crear nuevos usuarios.");
        }
        
        Organizacion organizacion = (Organizacion) usuarioActual;
        // Comprobar suscripciones activas antes de crear usuarios
        boolean tieneSuscripcionActiva = organizacion.getActivo() != null && organizacion.getActivo();
        if (!tieneSuscripcionActiva) {
            throw new AccessDeniedException("La organización no tiene una suscripción activa. No puede crear usuarios.");
        }
                 

        String rolUpper = request.getRol().toUpperCase();
        if ("ORGANIZACION".equals(rolUpper)) {
            throw new IllegalArgumentException("No se puede crear usuarios con rol ORGANIZACIÓN.");
        }

        
        if (usuarioRepository.existsByNombreUsuario(request.getUsername())) {
            throw new IllegalArgumentException("El username ' " + request.getUsername() + "' ya está registrado.");
        }


        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (usuarioRepository.existsByCorreoElectronico(request.getEmail())) {
                throw new IllegalArgumentException("El email ' " + request.getEmail() + "' ya está registrado.");
            }
        }

        
        Usuario nuevoUsuario = null;
        
        if ("MAESTRO".equals(rolUpper)) {
            nuevoUsuario = new Maestro(
                request.getNombre(),
                request.getPrimerApellido(),
                request.getSegundoApellido(),
                request.getUsername(),
                request.getEmail() != null ? request.getEmail() : "",
                passwordEncoder.encode(request.getPassword()),
                organizacion
            );
            organizacion.getMaestros().add((Maestro) nuevoUsuario);
            
        } else if ("ALUMNO".equals(rolUpper)) {
            nuevoUsuario = new Alumno(
                request.getNombre(),
                request.getPrimerApellido(),
                request.getSegundoApellido(),
                request.getUsername(),
                request.getEmail() != null ? request.getEmail() : "",
                passwordEncoder.encode(request.getPassword()),
                0,
                organizacion
            );
            organizacion.getAlumnos().add((Alumno) nuevoUsuario);
            
        } else {
            throw new IllegalArgumentException("Rol inválido. Use: MAESTRO o ALUMNO.");
        }

        usuarioRepository.save(nuevoUsuario);
        
    }

    private List<String> crearUsuarioImportacionMasiva(CreateUserRequest request, Integer fila) {
        List<String> errores = new ArrayList<>();
        
        Usuario usuarioActual = usuarioService.findCurrentUser();
        Organizacion organizacion = (Organizacion) usuarioActual;
                 
        String rolUpper = request.getRol().toUpperCase();
        if ("ORGANIZACION".equals(rolUpper)) {
            errores.add("No se puede crear usuarios con rol ORGANIZACIÓN. Error en fila " + fila + " del archivo.");
        }

        if(request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            errores.add("El nombre es obligatorio. Error en fila " + fila + " del archivo.");
        }

        if(request.getPrimerApellido() == null || request.getPrimerApellido().trim().isEmpty()) {
            errores.add("El primer apellido es obligatorio. Error en fila " + fila + " del archivo.");
        }

        if(request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            errores.add("El username es obligatorio. Error en fila " + fila + " del archivo.");
        } else if (usuarioRepository.existsByNombreUsuario(request.getUsername())) {
            errores.add("El username ' " + request.getUsername() + "' ya está registrado. Error en fila " + fila + " del archivo.");
        }

        if(request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            errores.add("La contraseña es obligatoria. Error en fila " + fila + " del archivo.");
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (usuarioRepository.existsByCorreoElectronico(request.getEmail())) {
                errores.add("El email ' " + request.getEmail() + "' ya está registrado. Error en fila " + fila + " del archivo.");
            } else if(!request.getEmail().contains("@")) {
                errores.add("El email ' " + request.getEmail() + "' no es válido. Error en fila " + fila + " del archivo.");
            }
        }

        
        Usuario nuevoUsuario = null;
        
        if ("MAESTRO".equals(rolUpper)) {
            nuevoUsuario = new Maestro(
                request.getNombre(),
                request.getPrimerApellido(),
                request.getSegundoApellido(),
                request.getUsername(),
                request.getEmail() != null ? request.getEmail() : "",
                passwordEncoder.encode(request.getPassword()),
                organizacion
            );
            
        } else if ("ALUMNO".equals(rolUpper)) {
            nuevoUsuario = new Alumno(
                request.getNombre(),
                request.getPrimerApellido(),
                request.getSegundoApellido(),
                request.getUsername(),
                request.getEmail() != null ? request.getEmail() : "",
                passwordEncoder.encode(request.getPassword()),
                0,
                organizacion
            );
            
        } else {
            errores.add("Rol inválido. Use: MAESTRO o ALUMNO. Error en fila " + fila + " del archivo.");
        }

        if(!errores.isEmpty()) {
            return errores;
        } else {
            usuarioRepository.save(nuevoUsuario);
            return errores;
        }
        
    }

    private Maestro toSafeMaestro(Maestro original) {
        Maestro maestro = new Maestro();
        maestro.setId(original.getId());
        maestro.setNombre(original.getNombre());
        maestro.setPrimerApellido(original.getPrimerApellido());
        maestro.setSegundoApellido(original.getSegundoApellido());
        maestro.setNombreUsuario(original.getNombreUsuario());
        maestro.setCorreoElectronico(original.getCorreoElectronico());
        maestro.setContrasena(original.getContrasena());
        return maestro;
    }

    private Alumno toSafeAlumno(Alumno original) {
        Alumno alumno = new Alumno();
        alumno.setId(original.getId());
        alumno.setNombre(original.getNombre());
        alumno.setPrimerApellido(original.getPrimerApellido());
        alumno.setSegundoApellido(original.getSegundoApellido());
        alumno.setNombreUsuario(original.getNombreUsuario());
        alumno.setCorreoElectronico(original.getCorreoElectronico());
        alumno.setContrasena(original.getContrasena());
        alumno.setPuntos(original.getPuntos());
        return alumno;
    }

    @Override
    @Transactional
    public List<String> leerArchivoImportacionUsuarios(MultipartFile archivo) throws ServletException, IOException {
        List<String> errores = new ArrayList<>();
        InputStream inputStream = archivo.getInputStream();
        Usuario usuarioActual = usuarioService.findCurrentUser();
        Organizacion organizacion = (Organizacion) usuarioActual;

        // Comprobar suscripciones activas antes de importar usuarios
        boolean tieneSuscripcionActiva = organizacion.getActivo() != null && organizacion.getActivo();
        if (!tieneSuscripcionActiva) {
            throw new AccessDeniedException("La organización no tiene una suscripción activa. No puede importar usuarios.");
        }
        
        if (!(usuarioActual instanceof Organizacion)) {
            throw new AccessDeniedException("Solo usuarios con rol ORGANIZACIÓN pueden crear nuevos usuarios.");
        }
        try (inputStream) {
            if(archivo.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                errores = leerArchivoCSV(inputStream);
            } else if (archivo.getOriginalFilename().toLowerCase().endsWith(".xlsx") || archivo.getOriginalFilename().toLowerCase().endsWith(".xls")) {
                errores = leerArchivoExcel(inputStream, archivo.getOriginalFilename());
            } else {
                throw new IllegalArgumentException("Archivo no soportado. Solo se permiten archivos .csv, .xlsx o .xls");
            }
        } catch (AccessDeniedException e) {
            throw new AccessDeniedException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            throw new ServletException("Error al procesar el archivo de importación: " + e.getMessage(), e);
        }
        return errores;
    }

    private List<String> leerArchivoCSV(InputStream inputStream) {
        List<String> errores = new ArrayList<>();
        Integer numFila = 0;
        try (Scanner scanner = new Scanner(inputStream, "UTF-8")) {
            while(scanner.hasNextLine()) {
                if(numFila <= 600) {
                    String linea = scanner.nextLine();
                    numFila++;
                    List<String> campos = Stream.of(linea.split(","))
                            .map(String::trim)
                            .toList();
                    if (numFila.equals(1) && (!campos.get(0).toLowerCase().equals("nombre") || 
                        !campos.get(1).toLowerCase().replace(" ", "").equals("primerapellido") || 
                        !campos.get(2).toLowerCase().replace(" ", "").equals("segundoapellido") || 
                        !campos.get(3).toLowerCase().replace(" ", "").equals("correoelectronico") || 
                        !campos.get(4).toLowerCase().replace(" ", "").equals("nombredeusuario") ||
                        !campos.get(5).toLowerCase().equals("contrasena") ||
                        !campos.get(6).toLowerCase().equals("rol"))) {
                            throw new IllegalArgumentException("El archivo CSV no tiene el formato correcto. La primera fila debe contener los encabezados: Nombre, Primer Apellido, Segundo Apellido, Correo Electronico, Nombre de Usuario, Contrasena, Rol");
                    } else if(numFila.equals(1)) {
                        continue; // Saltar la fila del encabezado
                    }
                    String nombre = campos.get(0);
                    String primerApellido = campos.get(1);
                    String segundoApellido = campos.get(2);
                    String correoElectronico = campos.get(3);
                    String nombreUsuario = campos.get(4);
                    String contrasena = campos.get(5);
                    String rol = campos.get(6);
                    CreateUserRequest request = new CreateUserRequest();
                    request.setNombre(nombre);
                    request.setPrimerApellido(primerApellido);
                    request.setSegundoApellido(segundoApellido);
                    request.setEmail(correoElectronico);
                    request.setUsername(nombreUsuario);
                    request.setPassword(contrasena);
                    request.setRol(rol);
                    errores.addAll(crearUsuarioImportacionMasiva(request, numFila));
                } else {
                    errores.add("El archivo CSV excede el límite de 600 filas. Se han procesado las primeras 600 filas.");
                    break;
                }
            }
        }
        return errores;
    }

    private List<String> leerArchivoExcel(InputStream inputStream, String nombreArchivo) throws IOException{
        List<String> errores = new ArrayList<>();
        Workbook workbook = null;
        try {
            if (nombreArchivo.toLowerCase().endsWith(".xls")) {
                workbook = new HSSFWorkbook(inputStream);  // Excel antiguo
            } else if (nombreArchivo.toLowerCase().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(inputStream);  // Excel moderno
            } else {
                throw new IllegalArgumentException("Formato de archivo no soportado: " + nombreArchivo);
            }
            Sheet sheet = workbook.getSheetAt(0); // Primera hoja
            boolean esPrimeraFila = true;

            for (Row row : sheet) {
                if((row.getRowNum()+1) <= 600) {
                    List<String> campos = new ArrayList<>();
                    for (int i = 0; i <= 6; i++) { // Esperamos 7 columnas (0 a 6)
                        Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        campos.add(obtenerCeldaComoTexto(cell).trim());
                    }
    
                    // Validar encabezado en la primera fila
                    if (esPrimeraFila) {
                        boolean hayEncabezado = !campos.stream().anyMatch(c -> c.toLowerCase().equals("profesor") || c.toLowerCase().equals("alumno"));
                        if (hayEncabezado) {
                            if (!campos.get(0).equalsIgnoreCase("nombre") ||
                                !campos.get(1).replace(" ", "").equalsIgnoreCase("primerapellido") ||
                                !campos.get(2).replace(" ", "").equalsIgnoreCase("segundoapellido") ||
                                !campos.get(3).replace(" ", "").equalsIgnoreCase("correoelectronico") ||
                                !campos.get(4).replace(" ", "").equalsIgnoreCase("nombredeusuario") ||
                                !campos.get(5).equalsIgnoreCase("contrasena") ||
                                !campos.get(6).equalsIgnoreCase("rol")) {
                                throw new IllegalArgumentException("El archivo Excel no tiene el formato correcto. La primera fila debe contener los encabezados: Nombre, Primer Apellido, Segundo Apellido, Correo Electrónico, Nombre de Usuario, Contraseña, Rol");
                            }
                            esPrimeraFila = false;
                            continue; // Saltar fila encabezado
                        } else {
                            throw new IllegalArgumentException("El archivo Excel no tiene el formato correcto. La primera fila debe contener los encabezados: Nombre, Primer Apellido, Segundo Apellido, Correo Electrónico, Nombre de Usuario, Contraseña, Rol");
                        }
                    } else {
                        // Procesar filas de datos
                        String nombre = campos.get(0);
                        String primerApellido = campos.get(1);
                        String segundoApellido = campos.get(2);
                        String correoElectronico = campos.get(3);
                        String nombreUsuario = campos.get(4);
                        String contrasena = campos.get(5);
                        String rol = campos.get(6);
    
                        CreateUserRequest request = new CreateUserRequest();
                        request.setNombre(nombre);
                        request.setPrimerApellido(primerApellido);
                        request.setSegundoApellido(segundoApellido);
                        request.setEmail(correoElectronico);
                        request.setUsername(nombreUsuario);
                        request.setPassword(contrasena);
                        request.setRol(rol);
    
                        errores.addAll(crearUsuarioImportacionMasiva(request, row.getRowNum() + 1));
                    }
                } else {
                    errores.add("El archivo Excel excede el límite de 600 filas. Se han procesado las primeras 600 filas.");
                    break;
                }
            }
        } finally {
            if (workbook != null) {
                workbook.close();  // Cerrar para liberar recursos
            }
        }
        return errores;
    }

    // Método auxiliar para obtener el valor de una celda como String
    private String obtenerCeldaComoTexto(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BLANK -> "";
            default -> "";
        };
    }
}

