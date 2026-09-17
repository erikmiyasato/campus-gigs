package br.com.fiap.campus_gigs.controller;

import br.com.fiap.campus_gigs.dto.ContratacaoRequest;
import br.com.fiap.campus_gigs.dto.ContratacaoResponse;
import br.com.fiap.campus_gigs.model.Usuario;
import br.com.fiap.campus_gigs.service.ContratacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contratacoes")
public class ContratacaoController {

    private final ContratacaoService contratacaoService;

    public ContratacaoController(ContratacaoService contratacaoService) {
        this.contratacaoService = contratacaoService;
    }

    @PostMapping
    public ResponseEntity<ContratacaoResponse> contratar(@RequestBody @Valid ContratacaoRequest request,
                                                         @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contratacaoService.contratar(request.servicoId(), usuario));
    }
}