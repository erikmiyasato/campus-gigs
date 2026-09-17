package br.com.fiap.campus_gigs.repository;

import br.com.fiap.campus_gigs.model.Contratacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratacaoRepository extends JpaRepository<Contratacao, Long> {}