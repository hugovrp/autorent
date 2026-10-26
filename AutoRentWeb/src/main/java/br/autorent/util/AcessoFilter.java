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
 * Restringe o acesso às páginas conforme a autenticação e o perfil do usuário. 
 */
public class AcessoFilter implements Filter {

	/** Verifica as permissões de acesso antes de encaminhar a requisição.
	 * 
	 * @param servletRequest requisição recebida pelo filtro.
	 * @param servletResponse resposta associada à requisição.
	 * @param chain cadeia de filtros a ser executada quando o acesso for permitido.
	 * @throws IOException se ocorrer falha de entrada ou saída.
	 * @throws ServletException se ocorrer falha no processamento da requisição.
	 */
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		String caminho = request.getServletPath(); 

		if (ehPublico(caminho)) {
			chain.doFilter(request, response);
			return;
		}

		HttpSession sessao = request.getSession(false);
		Usuario usuario = sessao == null ? null : (Usuario) sessao.getAttribute(SessaoMB.ATRIBUTO_USUARIO);

		if (usuario == null) {
			response.sendRedirect(request.getContextPath() + "/login.xhtml");
		} 
		else if (caminho.startsWith("/admin/") && usuario.getTipo() != TipoUsuario.FUNCIONARIO) {
			response.sendRedirect(request.getContextPath() + "/home.xhtml");
		} 
		else {
			chain.doFilter(request, response);
		}
	}

	/** Verifica se o caminho corresponde a uma página acessível sem autenticação.
	 * @param caminho caminho da página solicitada.
	 * @return {@code true} se a página for pública; caso contrário, {@code false}.
	 */
	private boolean ehPublico(String caminho) {
		return caminho.equals("/index.xhtml")
				|| caminho.equals("/login.xhtml")
				|| caminho.equals("/cadastro.xhtml")
				|| caminho.startsWith("/jakarta.faces.resource/");
	}
}
