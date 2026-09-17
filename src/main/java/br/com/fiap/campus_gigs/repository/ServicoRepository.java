package br.com.fiap.campus_gigs.repository;

import br.com.fiap.campus_gigs.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
}