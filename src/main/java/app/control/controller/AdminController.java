package app.control.controller;

import app.control.model.RegistroFichaje;
import app.control.repository.RegistroFichajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/api")  // Solo ADMIN
public class AdminController {

    @Autowired
    private RegistroFichajeRepository fichajeRepo;

    @GetMapping("/fichajes")
    public List<RegistroFichaje> listarFichajes() {
        return fichajeRepo.findAll();
    }

    @GetMapping("/ultimas-ubicaciones")
    public List<RegistroFichaje> ultimasUbicaciones() {
        return fichajeRepo.findUltimasUbicacionesDeEmpleados();
    }
}
