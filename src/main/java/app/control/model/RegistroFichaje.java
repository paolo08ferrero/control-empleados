package app.control.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registro_fichajes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroFichaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empleado_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Empleado empleado;

    @Enumerated(EnumType.STRING)
    private TipoFichaje tipo;

    private LocalDateTime fechaHora;

    private Double latitud;

    private Double longitud;
    private String edificio;
    private String direccion;
}
