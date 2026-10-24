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
 * Guarda o usuário autenticado durante toda a sessão HTTP.
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

	public String logout() {
		iUsuario.logout();
		usuario = null;
		request.getSession().invalidate();
		return "/login?faces-redirect=true";
	}

	public boolean isLogado() {
		return usuario != null;
	}

	/** Perfil administrador = funcionário da locadora. */
	public boolean isAdmin() {
		return usuario != null && usuario.getTipo() == TipoUsuario.FUNCIONARIO;
	}

	/** Perfil usuário comum = cliente. */
	public boolean isCliente() {
		return usuario != null && usuario.getTipo() == TipoUsuario.CLIENTE;
	}

	public Usuario getUsuario() {
		return usuario;
	}
}
