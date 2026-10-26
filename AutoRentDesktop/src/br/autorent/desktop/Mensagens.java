package br.autorent.desktop;

/** 
 * Converte falhas técnicas em mensagens para exibição na interface. 
 */
public final class Mensagens {

	private Mensagens() { }

	/** 
	 * Converte uma falha em uma mensagem adequada para exibição.
	 * 
	 * @param erro valor utilizado pela operação
	 * @return resultado da operação
	 */
	public static String de(Throwable erro) {
		erro.printStackTrace(); 

		Throwable raiz = erro;
		for (Throwable t = erro; t != null; t = t.getCause()) {
			if (t instanceof Exception) {
				return t.getMessage() != null && !t.getMessage().isBlank() ? t.getMessage()
						: "Não foi possível concluir a operação (" + t.getClass().getSimpleName() + ").";
			}
			if (t instanceof ClassNotFoundException || t instanceof NoClassDefFoundError) {
				return "Falta uma biblioteca no classpath do Desktop: " + t.getMessage() + ".";
			}
			raiz = t;
		}
		return "Falha na comunicação com o servidor " + ConexaoEJB.getEnderecoServidor() + " ("
				+ raiz.getClass().getSimpleName() + (raiz.getMessage() != null ? ": " + raiz.getMessage() : "") + ").";
	}
}