package br.autorent.desktop;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import br.autorent.sessionbeans.IAcessorio;
import br.autorent.sessionbeans.IAgencia;
import br.autorent.sessionbeans.ICliente;
import br.autorent.sessionbeans.IConsultaFrota;
import br.autorent.sessionbeans.ILocacao;
import br.autorent.sessionbeans.IOperacaoLocacao;
import br.autorent.sessionbeans.IStatusFrota;
import br.autorent.sessionbeans.IUsuario;
import br.autorent.sessionbeans.IVeiculo;

/** 
 * Centraliza a conexão e a localização dos EJBs remotos do backend. 
 */
public class ConexaoEJB {

	private static final String HOST = "localhost";
	private static final int PORTA = 8080;

	private static final String EAR = "AutoRent-EAR";
	private static final String MODULO_EJB = "AutoRent";

	private static Context context;

	public static String getEnderecoServidor() {
		return HOST + ":" + PORTA;
	}

	private static synchronized Context getContext() throws NamingException {
		if (context == null) {
			Properties props = new Properties();
			props.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
			props.put(Context.PROVIDER_URL, "remote+http://" + HOST + ":" + PORTA);
			props.put("jboss.naming.client.ejb.context", true);
			context = new InitialContext(props);
		}
		return context;
	}

	/** Localiza um EJB remoto pelo nome JNDI informado.
	 * 
	 * @param bean valor utilizado pela operação
	 * @param interfaceRemota valor utilizado pela operação
	 * @param stateful valor utilizado pela operação
	 * @return resultado da operação
	 */
	private static <T> T lookup(String bean, Class<T> interfaceRemota, boolean stateful) throws NamingException {
		String jndiName = "ejb:" + EAR + "/" + MODULO_EJB + "//" + bean + "!" + interfaceRemota.getName()
				+ (stateful ? "?stateful" : "");
		return interfaceRemota.cast(getContext().lookup(jndiName));
	}

	// Stateful: cada chamada cria uma nova sessão no servidor (guarde a referência retornada)
	public static IUsuario getUsuarioService() throws NamingException {
		return lookup("UsuarioSB", IUsuario.class, true);
	}

	public static IOperacaoLocacao getOperacaoLocacaoService() throws NamingException {
		return lookup("OperacaoLocacaoSB", IOperacaoLocacao.class, true);
	}

	// Stateless / Singleton
	public static ICliente getClienteService() throws NamingException {
		return lookup("ClienteSB", ICliente.class, false);
	}

	public static IVeiculo getVeiculoService() throws NamingException {
		return lookup("VeiculoSB", IVeiculo.class, false);
	}

	public static IAgencia getAgenciaService() throws NamingException {
		return lookup("AgenciaSB", IAgencia.class, false);
	}

	public static IAcessorio getAcessorioService() throws NamingException {
		return lookup("AcessorioSB", IAcessorio.class, false);
	}

	public static ILocacao getLocacaoService() throws NamingException {
		return lookup("LocacaoSB", ILocacao.class, false);
	}

	public static IConsultaFrota getConsultaFrotaService() throws NamingException {
		return lookup("ConsultaFrotaSB", IConsultaFrota.class, false);
	}

	public static IStatusFrota getStatusFrotaService() throws NamingException {
		return lookup("StatusFrotaSB", IStatusFrota.class, false);
	}
}
