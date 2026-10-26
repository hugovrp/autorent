package br.autorent.desktop;

import javax.naming.NamingException;

import br.autorent.dto.UsuarioLogadoDTO;
import br.autorent.modelo.enums.TipoUsuario;
import br.autorent.sessionbeans.IUsuario;

/** 
 * Gerencia a sessão do funcionário autenticado e seu serviço remoto. 
 */
public class SessaoFuncionario {

	public interface FabricaUsuarioService {
		IUsuario criar() throws NamingException;
	}

	private static final SessaoFuncionario INSTANCIA = new SessaoFuncionario(ConexaoEJB::getUsuarioService);

	private final FabricaUsuarioService fabrica;
	private IUsuario servico;
	private UsuarioLogadoDTO funcionario;

	SessaoFuncionario(FabricaUsuarioService fabrica) {
		this.fabrica = fabrica;
	}

	public static SessaoFuncionario getInstancia() {
		return INSTANCIA;
	}

	/** 
	 * Autentica o funcionário e inicia sua sessão.
	 * 
	 * @param cpf valor utilizado pela operação
	 * @param senha valor utilizado pela operação
	 * @return resultado da operação
	 */
	public synchronized UsuarioLogadoDTO entrar(String cpf, String senha) throws AutenticacaoException {
		IUsuario usuarioService = obterServico();

		try {
			if (!usuarioService.login(cpf, senha)) {
				throw new AutenticacaoException("CPF ou senha inválidos.");
			}

			UsuarioLogadoDTO autenticado = usuarioService.obterUsuarioLogado();
			if (autenticado == null || autenticado.getTipo() != TipoUsuario.FUNCIONARIO) {
				encerrarNoServidor(usuarioService);
				throw new AutenticacaoException("Acesso restrito a funcionários da locadora.");
			}

			funcionario = autenticado;
			return funcionario;
		} catch (AutenticacaoException e) {
			throw e;
		} catch (Exception | LinkageError e) {
			servico = null;
			funcionario = null;
			e.printStackTrace();
			throw new AutenticacaoException(mensagemDeFalha(e), e);
		}
	}

	/** 
	 * Encerra a sessão local do funcionário.
	 */
	public synchronized void sair() {
		if (servico != null) {
			encerrarNoServidor(servico);
		}
		funcionario = null;
	}

	public synchronized boolean isAutenticado() {
		return funcionario != null;
	}

	public synchronized UsuarioLogadoDTO getFuncionario() {
		return funcionario;
	}

	/** 
	 * Obtém o serviço remoto associado à sessão.
	 * 
	 * @return resultado da operação
	 */
	private IUsuario obterServico() throws AutenticacaoException {
		if (servico == null) {
			try {
				servico = fabrica.criar();
			} catch (Exception e) {
				throw new AutenticacaoException("Não foi possível localizar o serviço de autenticação em "
						+ ConexaoEJB.getEnderecoServidor() + ". Verifique se o servidor está em execução e o EAR publicado.", e);
			}
		}
		return servico;
	}

	/** 
	 * Encerra a sessão no serviço remoto.
	 * 
	 * @param usuarioService valor utilizado pela operação
	 */
	private void encerrarNoServidor(IUsuario usuarioService) {
		try {
			usuarioService.logout();
		} 
		catch (Exception e) {
			// o logout é "melhor esforço": a sessão do servidor expira sozinha
		}
	}

	/** 
	 * Obtém uma mensagem adequada para uma falha de autenticação.
	 * 
	 * @param erro valor utilizado pela operação
	 * @return resultado da operação
	 */
	private String mensagemDeFalha(Throwable erro) {
		Throwable raiz = erro;
		for (Throwable t = erro; t != null; t = t.getCause()) {
			if (t instanceof ClassNotFoundException || t instanceof NoClassDefFoundError) {
				return "Falta uma biblioteca no classpath do Desktop: " + t.getMessage() + ". Consulte o LEIA-ME do projeto.";
			}
			raiz = t;
		}
		return "Falha na comunicação com o servidor " + ConexaoEJB.getEnderecoServidor() + " ("
				+ raiz.getClass().getSimpleName() + (raiz.getMessage() != null ? ": " + raiz.getMessage() : "") + ").";
	}
}