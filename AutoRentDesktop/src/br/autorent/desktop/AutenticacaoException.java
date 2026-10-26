package br.autorent.desktop;

/** 
 * Representa uma falha durante a autenticação do funcionário. 
 */
public class AutenticacaoException extends Exception {
	private static final long serialVersionUID = 1L;

	public AutenticacaoException(String mensagem) {
		super(mensagem);
	}

	public AutenticacaoException(String mensagem, Throwable causa) {
		super(mensagem, causa);
	}
}