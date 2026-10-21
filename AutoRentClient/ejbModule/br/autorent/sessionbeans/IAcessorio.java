package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.modelo.Acessorio;

public interface IAcessorio extends ICrud<Acessorio, Long> {
	/** Acessórios cuja descrição contém o texto informado. */
	List<Acessorio> findByDescricao(String descricao);

	/** Acessórios instalados em um veículo. */
	List<Acessorio> findByVeiculo(Long veiculoId);
}
