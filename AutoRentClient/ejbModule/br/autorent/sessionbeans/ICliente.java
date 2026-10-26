package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.dto.ClienteDTO;
import br.autorent.modelo.Usuario;

/**
 * Define o contrato de operações de cliente disponibilizadas pelo backend.
 */
public interface ICliente extends ICrud<Usuario, Long> {
	
	/**
	 * Busca um usuário pelo CPF.
	 * 
	 * @param cpf valor utilizado para cpf.
	 * @return objeto ou resultado correspondente à operação.
	 */
	Usuario buscarPorCpf(String cpf);

	/**
	 * Busca um usuário pelo e-mail.
	 * 
	 * @param email valor utilizado para email.
	 * @return objeto ou resultado correspondente à operação.
	 */
	Usuario buscarPorEmail(String email);

	/**
	 * Busca registros pelo nome.
	 * 
	 * @param nome valor utilizado para nome.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<Usuario> buscarPorNome(String nome);

	/**
	 * Lista os clientes cadastrados.
	 * 
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<ClienteDTO> listarClientes();

	/**
	 * Pesquisa clientes por nome, e-mail ou CPF.
	 * 
	 * @param texto valor utilizado para texto.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<ClienteDTO> pesquisarClientes(String texto);

	/**
	 * Valida e cadastra um cliente.
	 * 
	 * @param nome valor utilizado para nome.
	 * @param cpf valor utilizado para cpf.
	 * @param email valor utilizado para email.
	 * @param senha valor utilizado para senha.
	 * @return objeto ou resultado correspondente à operação.
	 */
	ClienteDTO cadastrarCliente(String nome, String cpf, String email, String senha) throws Exception;
}

