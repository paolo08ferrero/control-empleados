package app.control.util; // ajusta según la carpeta donde lo pongas

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerarHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String passwordHash = encoder.encode("ceo3533");
        System.out.println(passwordHash);
    }
}
