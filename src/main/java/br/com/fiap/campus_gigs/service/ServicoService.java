package br.com.fiap.campus_gigs.service;

import br.com.fiap.campus_gigs.dto.ServicoRequest;
import br.com.fiap.campus_gigs.dto.ServicoResponse;
import br.com.fiap.campus_gigs.model.Papel;
import br.com.fiap.campus_gigs.model.Servico;
import br.com.fiap.campus_gigs.model.SituacaoServico;
import br.com.fiap.campus_gigs.model.Usuario;
import br.com.fiap.campus_gigs.repository.ServicoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;

    public ServicoService(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    public ServicoResponse cadastrar(ServicoRequest request, Usuario prestador) {
        Servico servico = new Servico();
        servico.setPrestador(prestador);
        servico.setTitulo(request.titulo());
        servico.setDescricao(request.descricao());
        servico.setCategoria(request.categoria());
        servico.setPreco(request.preco());
        servico.setSituacao(SituacaoServico.ATIVO);

        return ServicoResponse.from(servicoRepository.save(servico));
    }

    @Transactional(readOnly = true)
    public List<ServicoResponse> listar() {
        return servicoRepository.findAll().stream().map(ServicoResponse::from).toList();
    }

    public ServicoResponse encerrar(Long id, Usuario usuarioLogado) {
        Servico servico = buscarEntidadePorId(id);

        boolean isDono = servico.getPrestador().getId().equals(usuarioLogado.getId());
        boolean isAdmin = usuarioLogado.getPapel() == Papel.ADMIN;

        if (!isDono && !isAdmin) {
            throw new AccessDeniedException("Não autorizado a encerrar este serviço");
        }

        servico.setSituacao(SituacaoServico.ENCERRADO);
        return ServicoResponse.from(servicoRepository.save(servico));
    }

    public Servico buscarEntidadePorId(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado"));
    }
}