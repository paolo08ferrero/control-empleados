package app.control.controller;

import app.control.model.Empleado;
import app.control.model.RegistroFichaje;
import app.control.model.TipoFichaje;
import app.control.repository.EmpleadoRepository;
import app.control.repository.RegistroFichajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@RestController
@RequestMapping("/api")
public class ControlEmpleadosController {

    private final EmpleadoRepository empleadoRepo;
    private final RegistroFichajeRepository fichajeRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ControlEmpleadosController(EmpleadoRepository empleadoRepo,
                                      RegistroFichajeRepository fichajeRepo,
                                      PasswordEncoder passwordEncoder) {
        this.empleadoRepo = empleadoRepo;
        this.fichajeRepo = fichajeRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------------- LOGIN ----------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String usuario, @RequestParam String contraseña) {
        Optional<Empleado> empleadoOpt = empleadoRepo.findByUsuario(usuario);
        if (empleadoOpt.isPresent() && passwordEncoder.matches(contraseña, empleadoOpt.get().getContraseña())) {
            Empleado empleado = empleadoOpt.get();

            // Crear token con rol
            UsernamePasswordAuthenticationToken authToken
                    = new UsernamePasswordAuthenticationToken(
                            empleado.getUsuario(),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + empleado.getRol()))
                    );

            SecurityContextHolder.getContext().setAuthentication(authToken);

            Map<String, Object> datos = new HashMap<>();
            datos.put("id", empleado.getId());
            datos.put("nombre", empleado.getNombre());
            datos.put("usuario", empleado.getUsuario());
            datos.put("rol", empleado.getRol());
            return ResponseEntity.ok(datos);
        }
        return ResponseEntity.status(401).body("Credenciales incorrectas");
    }

    // ---------------- CREAR EMPLEADO ----------------
    @PostMapping("/empleados")
    public ResponseEntity<?> crearEmpleado(@RequestBody Empleado nuevoEmpleado) {
        if (empleadoRepo.findByUsuario(nuevoEmpleado.getUsuario()).isPresent()) {
            return ResponseEntity.badRequest().body("El usuario ya existe");
        }
        nuevoEmpleado.setContraseña(passwordEncoder.encode(nuevoEmpleado.getContraseña()));
        Empleado empleadoGuardado = empleadoRepo.save(nuevoEmpleado);
        return ResponseEntity.ok(empleadoGuardado);
    }

    // ---------------- LISTAR EMPLEADOS ----------------
    @GetMapping("/empleados")
    public List<Empleado> listarEmpleados() {
        return empleadoRepo.findAll();
    }

    // ---------------- EDITAR EMPLEADO ----------------
    @PutMapping("/empleados/{id}")
    public ResponseEntity<?> editarEmpleado(@PathVariable Long id, @RequestBody Empleado datosActualizados) {
        Optional<Empleado> empleadoOpt = empleadoRepo.findById(id);
        if (empleadoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Empleado no encontrado");
        }
        Empleado empleado = empleadoOpt.get();
        empleado.setNombre(datosActualizados.getNombre());
        empleado.setUsuario(datosActualizados.getUsuario());
        empleado.setContraseña(passwordEncoder.encode(datosActualizados.getContraseña()));
        empleado.setRol(datosActualizados.getRol());
        empleado.setEdificio(datosActualizados.getEdificio());
        empleado.setDireccion(datosActualizados.getDireccion());
        empleadoRepo.save(empleado);
        return ResponseEntity.ok(empleado);
    }

    // ---------------- ELIMINAR EMPLEADO ----------------
    @DeleteMapping("/empleados/{id}")
    public ResponseEntity<?> eliminarEmpleado(@PathVariable Long id) {
        if (empleadoRepo.existsById(id)) {
            empleadoRepo.deleteById(id);
            return ResponseEntity.ok("Empleado eliminado correctamente");
        }
        return ResponseEntity.badRequest().body("Empleado no encontrado");
    }

    // ---------------- FICHAR ----------------
    @PostMapping("/fichar")
    public ResponseEntity<?> fichar(@RequestParam Long empleadoId, @RequestParam String tipo,
                                    @RequestParam double latitud, @RequestParam double longitud) {
        Optional<Empleado> empleadoOpt = empleadoRepo.findById(empleadoId);
        if (empleadoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Empleado no encontrado");
        }

        TipoFichaje nuevoTipo;
        try {
            nuevoTipo = TipoFichaje.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Tipo de fichaje inválido. Debe ser 'ENTRADA' o 'SALIDA'");
        }

        Optional<RegistroFichaje> ultimoFichajeOpt = fichajeRepo.findTopByEmpleadoIdOrderByFechaHoraDesc(empleadoId);
        if (ultimoFichajeOpt.isPresent()) {
            TipoFichaje ultimoTipo = ultimoFichajeOpt.get().getTipo();
            if (nuevoTipo == TipoFichaje.ENTRADA && ultimoTipo == TipoFichaje.ENTRADA) {
                return ResponseEntity.badRequest()
                        .body("Ya existe un ingreso activo. Debe hacer la salida antes de ingresar nuevamente.");
            }
            if (nuevoTipo == TipoFichaje.SALIDA && ultimoTipo == TipoFichaje.SALIDA) {
                return ResponseEntity.badRequest()
                        .body("Ya existe una salida registrada. Debe hacer un nuevo ingreso antes de salir.");
            }
        } else {
            if (nuevoTipo == TipoFichaje.SALIDA) {
                return ResponseEntity.badRequest()
                        .body("No puede registrar salida sin un ingreso previo.");
            }
        }

        RegistroFichaje fichaje = new RegistroFichaje();
        fichaje.setEmpleado(empleadoOpt.get());
        fichaje.setTipo(nuevoTipo);
        fichaje.setFechaHora(LocalDateTime.now());
        fichaje.setLatitud(latitud);
        fichaje.setLongitud(longitud);
        fichajeRepo.save(fichaje);

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Fichaje registrado correctamente");
        response.put("latitudRegistrada", latitud);
        response.put("longitudRegistrada", longitud);
        return ResponseEntity.ok(response);
    }

    // ---------------- ACTUALIZAR UBICACIÓN ----------------
    @PostMapping("/actualizar-ubicacion")
    public ResponseEntity<?> actualizarUbicacion(@RequestParam Long empleadoId,
                                                 @RequestParam double latitud,
                                                 @RequestParam double longitud) {
        Optional<Empleado> empleadoOpt = empleadoRepo.findById(empleadoId);
        if (empleadoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Empleado no encontrado");
        }

        Optional<RegistroFichaje> ultimoFichajeOpt = fichajeRepo.findTopByEmpleadoIdOrderByFechaHoraDesc(empleadoId);
        if (ultimoFichajeOpt.isEmpty() || ultimoFichajeOpt.get().getTipo() != TipoFichaje.ENTRADA) {
            return ResponseEntity.badRequest().body("No hay fichaje de entrada activo para actualizar ubicación.");
        }

        Optional<RegistroFichaje> ultimaUbicacionOpt
                = fichajeRepo.findTopByEmpleadoIdAndTipoOrderByFechaHoraDesc(empleadoId, TipoFichaje.UBICACION);

        RegistroFichaje ubicacion;
        if (ultimaUbicacionOpt.isPresent()) {
            ubicacion = ultimaUbicacionOpt.get();
            ubicacion.setLatitud(latitud);
            ubicacion.setLongitud(longitud);
            ubicacion.setFechaHora(LocalDateTime.now());
        } else {
            ubicacion = new RegistroFichaje();
            ubicacion.setEmpleado(empleadoOpt.get());
            ubicacion.setTipo(TipoFichaje.UBICACION);
            ubicacion.setLatitud(latitud);
            ubicacion.setLongitud(longitud);
            ubicacion.setFechaHora(LocalDateTime.now());
        }

        fichajeRepo.save(ubicacion);
        return ResponseEntity.ok("Ubicación actualizada correctamente");
    }

    // ---------------- OBTENER ÚLTIMA UBICACIÓN ----------------
    @GetMapping("/ultima-ubicacion/{empleadoId}")
    public ResponseEntity<?> obtenerUltimaUbicacion(@PathVariable Long empleadoId) {
        Optional<RegistroFichaje> ultimoRegistroOpt
                = fichajeRepo.findTopByEmpleadoIdAndTipoOrderByFechaHoraDesc(empleadoId, TipoFichaje.UBICACION);

        if (ultimoRegistroOpt.isEmpty()) {
            return ResponseEntity.ok("No hay registros de ubicación para este empleado");
        }
        return ResponseEntity.ok(ultimoRegistroOpt.get());
    }

    // ---------------- OBTENER UBICACIONES DE TODOS ----------------
    @GetMapping("/ubicaciones-actuales")
    public List<RegistroFichaje> obtenerUltimasUbicaciones() {
        return fichajeRepo.findUltimasUbicacionesDeEmpleados();
    }

    // ---------------- LISTAR FICHAJES ----------------
    @GetMapping("/fichajes-entrada-salida")
    public List<RegistroFichaje> fichajesEntradaSalida() {
        return fichajeRepo.findAllOrderByFechaHoraDesc();
    }

    @GetMapping("/fichajes-por-empleado/{empleadoId}")
    public List<RegistroFichaje> fichajesPorEmpleado(@PathVariable Long empleadoId) {
        return fichajeRepo.findByEmpleadoIdOrderByFechaHoraDesc(empleadoId);
    }
    @GetMapping("/empleado-logueado")
public ResponseEntity<?> empleadoLogueado() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
        return ResponseEntity.status(401).body("No logueado");
    }

    // Suponiendo que tu principal es el username
    String username = auth.getName();
    var empleadoOpt = empleadoRepo.findByUsuario(username);
    if (empleadoOpt.isEmpty()) {
        return ResponseEntity.status(404).body("Empleado no encontrado");
    }
    return ResponseEntity.ok(empleadoOpt.get());
}


    @GetMapping("/fichajes-filtrados")
    public List<RegistroFichaje> fichajesFiltrados(
            @RequestParam(required = false) Long empleadoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        if (fecha != null) {
            return fichajeRepo.findByEmpleadoIdAndFecha(empleadoId, fecha);
        } else if (empleadoId != null) {
            return fichajeRepo.findByEmpleadoIdOrderByFechaHoraDesc(empleadoId);
        } else {
            return fichajeRepo.findAllOrderByFechaHoraDesc();
        }
    }
}
