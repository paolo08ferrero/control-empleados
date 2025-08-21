package app.control.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FichajeController {

    @GetMapping("/fichaje")
    public String fichaje() {
        return "fichaje";  // Spring buscará templates/fichaje.html
    }
}
