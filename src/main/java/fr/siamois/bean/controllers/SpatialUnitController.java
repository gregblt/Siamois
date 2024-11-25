package fr.siamois.bean.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class SpatialUnitController {

    @GetMapping("/spatial-unit/{id}")
    public String getSpatialUnit(@PathVariable("id") String id, Model model) {
        // Add the 'id' parameter to the model for use in the view
        model.addAttribute("id", id);
        return "forward:/pages/spatialUnit/spatialUnit.xhtml?id="+id; // Forward to the XHTML view
    }
}
