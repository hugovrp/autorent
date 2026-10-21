package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.modelo.Agencia;

public interface IAgencia extends ICrud<Agencia, Long> {
	/** Agências de uma cidade. */
	List<Agencia> findByCidade(String cidade);

	/** Agências cujo nome contém o texto informado. */
	List<Agencia> findByNome(String nome);
}
