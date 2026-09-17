package br.com.fiap.campus_gigs.service;

import br.com.fiap.campus_gigs.dto.ContratacaoResponse;
import br.com.fiap.campus_gigs.model.*;
import br.com.fiap.campus_gigs.repository.ContratacaoRepository;
import org.springframework.stereotype.Service;

@Service
public class ContratacaoService {

    private final ContratacaoRepository contratacaoRepository;
    private final ServicoService servicoService;

    public ContratacaoService(ContratacaoRepository contratacaoRepository, ServicoService servicoService) {
        this.contratacaoRepository = contratacaoRepository;
        this.servicoService = servicoService;
    }

    public ContratacaoResponse contratar(Long servicoId, Usuario contratante) {
        Servico servico = servicoService.buscarEntidadePorId(servicoId);

        if (servico.getSituacao() != SituacaoServico.ATIVO) {
            throw new IllegalStateException("Serviço não está ativo para contratação");
        }

        if (servico.getPrestador().getId().equals(contratante.getId())) {
            throw new IllegalArgumentException("Não é permitido contratar o próprio serviço");
        }

        Contratacao contratacao = new Contratacao();
        contratacao.setServico(servico);
        contratacao.setContratante(contratante);
        contratacao.setSituacao(SituacaoContratacao.SOLICITADA);

        return ContratacaoResponse.from(contratacaoRepository.save(contratacao));
    }
}