package br.com.fiap.campus_gigs.dto;

import br.com.fiap.campus_gigs.model.Contratacao;
import br.com.fiap.campus_gigs.model.SituacaoContratacao;

import java.time.LocalDateTime;

public record ContratacaoResponse(
        Long id, Long servicoId, String servicoTitulo,
        Long contratanteId, SituacaoContratacao situacao, LocalDateTime criadoEm
) {
    public static ContratacaoResponse from(Contratacao c) {
        return new ContratacaoResponse(
                c.getId(), c.getServico().getId(), c.getServico().getTitulo(),
                c.getContratante().getId(), c.getSituacao(), c.getCriadoEm()
        );
    }
}