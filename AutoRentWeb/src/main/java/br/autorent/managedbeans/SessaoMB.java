package br.autorent.managedbeans;

import java.io.Serializable;

import br.autorent.modelo.Usuario;
import br.autorent.modelo.enums.TipoUsuario;
import br.autorent.sessionbeans.IUsuario;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

/** 
 * Mantém os dados do usuário autenticado durante a sessão HTTP. 
 */
@Named("sessaoMB")
@SessionScoped
public class SessaoMB implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String ATRIBUTO_USUARIO = "usuarioLogado";

	@EJB
	private IUsuario iUsuario;

	@Inject
	private HttpServletRequest request;

	private Usuario usuario;

	/** 
	 * Autentica o usuário e registra seus dados na sessão HTTP.
	 * 
	 * @param cpf CPF informado para autenticação.
	 * @param senha senha informada para autenticação.
	 * @return {@code true} se a autenticação for bem-sucedida; caso contrário, {@code false}.
	 */
	public boolean login(String cpf, String senha) {
		if (!iUsuario.login(cpf, senha)) {
			usuario = null;
			return false;
		}
		
		usuario = iUsuario.obterClienteLogado();
		request.changeSessionId();
		request.getSession().setAttribute(ATRIBUTO_USUARIO, usuario);
		
		return true;
	}

	/** 
	 * Encerra a autenticação e invalida a sessão HTTP atual.
	 * 
	 * @return caminho da página de login.
	 */
	public String logout() {
		iUsuario.logout();
		usuario = null;
		request.getSession().invalidate();
		return "/login?faces-redirect=true";
	}

	/** 
	 * Informa se existe um usuário autenticado na sessão. 
	 */
	public boolean isLogado() {
		return usuario != null;
	}
	
	public boolean isAdmin() {
		return usuario != null && usuario.getTipo() == TipoUsuario.FUNCIONARIO;
	}
	
	public boolean isCliente() {
		return usuario != null && usuario.getTipo() == TipoUsuario.CLIENTE;
	}

	public Usuario getUsuario() {
		return usuario;
	}
}

