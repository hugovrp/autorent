package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.modelo.Usuario;

public interface ICliente extends ICrud<Usuario, Long> {
	/** Busca pelo CPF (aceita com ou sem pontuação); devolve null se não existir. */
	Usuario findByCpf(String cpf);

	/** Busca pelo e-mail; devolve null se não existir. */
	Usuario findByEmail(String email);

	/** Clientes cujo nome contém o texto informado. */
	List<Usuario> findByNome(String nome);
}
