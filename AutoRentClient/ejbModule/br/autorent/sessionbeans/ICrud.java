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
	
	/**
	 * Persiste uma nova entidade.
	 * 
	 * @param entidade valor utilizado para entidade.
	 * @return objeto ou resultado correspondente à operação.
	 */
	T criar(T entidade);

	/**
	 * Atualiza os dados de uma entidade existente.
	 * 
	 * @param entidade valor utilizado para entidade.
	 * @return objeto ou resultado correspondente à operação.
	 */
	T atualizar(T entidade);

	/**
	 * Remove uma entidade pelo identificador.
	 * 
	 * @param id valor utilizado para id.
	 */
	void excluir(ID id);

	/**
	 * Busca uma entidade pelo identificador.
	 * 
	 * @param id valor utilizado para id.
	 * @return objeto ou resultado correspondente à operação.
	 */
	T buscarPorId(ID id);

	/**
	 * Lista todas as entidades.
	 * 
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<T> buscarTodos();
}
