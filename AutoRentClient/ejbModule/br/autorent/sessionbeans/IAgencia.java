package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.dto.AgenciaDTO;
import br.autorent.modelo.Agencia;
import br.autorent.modelo.enums.CategoriaVeiculo;

/**
 * Define o contrato de operações de agência disponibilizadas pelo backend.
 */
public interface IAgencia extends ICrud<Agencia, Long> {
	
	/**
	 * Busca agências pela cidade.
	 * 
	 * @param cidade valor utilizado para cidade.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<Agencia> buscarPorCidade(String cidade);

	/**
	 * Busca registros pelo nome.
	 * 
	 * @param nome valor utilizado para nome.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<Agencia> buscarPorNome(String nome);

	/**
	 * Lista agências com veículos disponíveis da categoria informada.
	 * 
	 * @param categoria valor utilizado para categoria.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<Agencia> buscarVeiculosDisponiveis(CategoriaVeiculo categoria);

	/**
	 * Pesquisa agências pelo nome ou cidade.
	 * 
	 * @param texto valor utilizado para texto.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<AgenciaDTO> pesquisarAgencias(String texto);

	/**
	 * Valida e cadastra uma agência.
	 * 
	 * @param nome valor utilizado para nome.
	 * @param endereco valor utilizado para endereco.
	 * @param cidade valor utilizado para cidade.
	 * @param telefone valor utilizado para telefone.
	 * @return objeto ou resultado correspondente à operação.
	 */
	AgenciaDTO cadastrarAgencia(String nome, String endereco, String cidade, String telefone) throws Exception;
}

