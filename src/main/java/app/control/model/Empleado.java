package app.control.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "empleados")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El usuario es obligatorio")
    @Column(unique = true)
    private String usuario;

    @NotBlank(message = "La contraseña es obligatoria")
    private String contraseña; 

    @NotBlank(message = "El rol es obligatorio")
    private String rol;  

    private String edificio; // no obligatorio
    private String direccion; // no obligatorio

    @OneToMany(mappedBy = "empleado", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<RegistroFichaje> fichajes;
}
