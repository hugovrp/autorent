package br.autorent.sessionbeans;

import java.io.Serializable;
import java.util.List;

/**
 *  Contrato genérico de CRUD, herdado por todas as interfaces remotas.
 *
 *  @param <T>  tipo da entidade
 *  @param <ID> tipo do identificador da entidade
 */
public interface ICrud<T, ID extends Serializable> {
	/** Persiste uma nova entidade (id deve ser nulo) e devolve a entidade salva. */
	T criar(T entidade);

	/** Atualiza a entidade identificada pelo id e devolve a versão atualizada. */
	T atualizar(T entidade);

	/** Remove a entidade pelo id. */
	void excluir(ID id);

	/** Busca por id; devolve null se não existir. */
	T buscarPorId(ID id);

	/** Lista todas as entidades. */
	List<T> buscarTodos();
}
