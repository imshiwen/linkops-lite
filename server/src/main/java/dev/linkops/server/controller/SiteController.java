package dev.linkops.server.controller;

import dev.linkops.server.dto.SiteForm;
import dev.linkops.server.service.SiteService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sites")
public class SiteController {

    private final SiteService service;

    public SiteController(SiteService service) {
        this.service = service;
    }

    @GetMapping
    public List<SiteForm> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public SiteForm get(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping
    public SiteForm create(@Valid @RequestBody SiteForm form) {
        return service.create(form);
    }

    @PutMapping("/{id}")
    public SiteForm update(@PathVariable long id, @Valid @RequestBody SiteForm form) {
        return service.update(id, form);
    }
}
