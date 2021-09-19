package wass.chess;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
public class ClientController {

    @RequestMapping("/")
    String index() {
        return "index";
    }

    @RequestMapping("/play")
    String play(@RequestParam("roomId") UUID roomId, @RequestParam("color") String color, Model model) {

        model.addAttribute("roomId", roomId);
        model.addAttribute("color", color);

        return "play";
    }

}
