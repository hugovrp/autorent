package br.autorent.util;

import java.io.IOException;

import br.autorent.managedbeans.SessaoMB;
import br.autorent.modelo.Usuario;
import br.autorent.modelo.enums.TipoUsuario;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controle de acesso por perfil (declarado no web.xml):
 *  - páginas públicas: index, login e cadastro;
 *  - demais páginas: exigem usuário autenticado;
 *  - /admin/*: exigem perfil administrador (FUNCIONARIO).
 */
public class AcessoFilter implements Filter {

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		String caminho = request.getRequestURI().substring(request.getContextPath().length());

		if (ehPublico(caminho)) {
			chain.doFilter(request, response);
			return;
		}

		HttpSession sessao = request.getSession(false);
		Usuario usuario = sessao == null ? null : (Usuario) sessao.getAttribute(SessaoMB.ATRIBUTO_USUARIO);

		if (usuario == null) {
			response.sendRedirect(request.getContextPath() + "/login.xhtml");
		} else if (caminho.startsWith("/admin/") && usuario.getTipo() != TipoUsuario.FUNCIONARIO) {
			response.sendRedirect(request.getContextPath() + "/home.xhtml");
		} else {
			chain.doFilter(request, response);
		}
	}

	private boolean ehPublico(String caminho) {
		return caminho.equals("/index.xhtml")
				|| caminho.equals("/login.xhtml")
				|| caminho.equals("/cadastro.xhtml")
				|| caminho.startsWith("/jakarta.faces.resource/");
	}
}