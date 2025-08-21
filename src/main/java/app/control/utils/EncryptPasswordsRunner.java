package app.control.utils;

import app.control.model.Empleado;
import app.control.repository.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EncryptPasswordsRunner implements CommandLineRunner {

    @Autowired
    private EmpleadoRepository empleadoRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        List<Empleado> empleados = empleadoRepo.findAll();
        for (Empleado emp : empleados) {
            // Verificamos si la contraseña ya está encriptada (opcional)
            if (!emp.getContraseña().startsWith("$2a$")) { // BCrypt siempre empieza con $2a$
                String rawPassword = emp.getContraseña();
                emp.setContraseña(passwordEncoder.encode(rawPassword));
                empleadoRepo.save(emp);
                System.out.println("Contraseña encriptada para usuario: " + emp.getUsuario());
            }
        }
        System.out.println("Todas las contraseñas existentes han sido encriptadas.");
    }
}
