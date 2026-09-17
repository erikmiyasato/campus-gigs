package br.com.fiap.campus_gigs.controller;

import br.com.fiap.campus_gigs.dto.ServicoRequest;
import br.com.fiap.campus_gigs.dto.ServicoResponse;
import br.com.fiap.campus_gigs.model.Usuario;
import br.com.fiap.campus_gigs.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @PostMapping
    public ResponseEntity<ServicoResponse> cadastrar(@RequestBody @Valid ServicoRequest request,
                                                     @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicoService.cadastrar(request, usuario));
    }

    @GetMapping
    public ResponseEntity<List<ServicoResponse>> listar() {
        return ResponseEntity.ok(servicoService.listar());
    }

    @PatchMapping("/{id}/encerrar")
    public ResponseEntity<ServicoResponse> encerrar(@PathVariable Long id,
                                                    @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(servicoService.encerrar(id, usuario));
    }
}