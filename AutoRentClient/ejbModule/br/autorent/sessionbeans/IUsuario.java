package br.autorent.sessionbeans;

import br.autorent.dto.UsuarioLogadoDTO;
import br.autorent.modelo.Usuario;

/**
 * Define o contrato de operações de usuario disponibilizadas pelo backend.
 */
public interface IUsuario {
	/**
	 * Autentica o usuário com as credenciais informadas.
	 * 
	 * @param cpf valor utilizado para cpf.
	 * @param senha valor utilizado para senha.
	 * @return true se a operação for bem-sucedida; caso contrário, false.
	 */
	boolean login(String cpf, String senha);
	
    /**
     * Encerra a sessão do usuário autenticado.
     */
    void logout();
    
    /**
     * Retorna o cliente atualmente autenticado.
     * 
     * @return objeto ou resultado correspondente à operação.
     */
    Usuario obterClienteLogado();
    

    /**
     * Retorna os dados básicos do usuário autenticado.
     * 
     * @return objeto ou resultado correspondente à operação.
     */
    UsuarioLogadoDTO obterUsuarioLogado();
}
