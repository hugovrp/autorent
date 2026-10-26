package br.autorent.sessionbeans;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import br.autorent.modelo.Usuario;
import jakarta.persistence.EntityManager;

/**
 * Disponibiliza funções para gerar e verificar hashes de senhas.
 */
final class SenhaUtil {
	private static final String PREFIXO = "pbkdf2";
	private static final int ITERACOES = 120_000;
	private static final int TAMANHO_SAL = 16;
	private static final int TAMANHO_HASH_BITS = 256;
	private static final SecureRandom ALEATORIO = new SecureRandom();

	private SenhaUtil() { }
	
	/**
	 * Gera o resumo criptográfico da senha informada.
	 * 
	 * @param senha parâmetro utilizado na operação.
	 * @return resultado da operação.
	 */
	static String hash(String senha) {
		byte[] sal = new byte[TAMANHO_SAL];
		ALEATORIO.nextBytes(sal);
		return PREFIXO + "$" + ITERACOES + "$" + Base64.getEncoder().encodeToString(sal) + "$"
				+ Base64.getEncoder().encodeToString(derivar(senha, sal, ITERACOES));
	}
	
	/**
	 * Verifica se o valor possui o formato de um hash.
	 * 
	 * @param valor parâmetro utilizado na operação.
	 * @return resultado da operação.
	 */
	static boolean ehHash(String valor) {
		if (valor == null)
			return false;

		String[] partes = valor.split("\\$");
		return partes.length == 4 && PREFIXO.equals(partes[0]) && partes[1].matches("\\d{4,7}");
	}
	
	/**
	 * Verifica se a senha corresponde ao hash informado.
	 * 
	 * @param senha parâmetro utilizado na operação.
	 * @param armazenado parâmetro utilizado na operação.
	 * @return resultado da operação.
	 */
	static boolean confere(String senha, String armazenado) {
		if (senha == null || !ehHash(armazenado))
			return false;

		try {
			String[] partes = armazenado.split("\\$");
			byte[] esperado = Base64.getDecoder().decode(partes[3]);
			byte[] calculado = derivar(senha, Base64.getDecoder().decode(partes[2]), Integer.parseInt(partes[1]));
			return MessageDigest.isEqual(esperado, calculado);
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * Confere a senha de um usuário. Senhas antigas, ainda gravadas em texto puro, continuam valendo uma última vez:
	 * se a senha estiver correta, o registro é regravado já com hash. Verifica a senha e atualiza seu hash quando necessário.
	 * 
	 * @param em parâmetro utilizado na operação.
	 * @param usuario parâmetro utilizado na operação.
	 * @param senha parâmetro utilizado na operação.
	 * @return resultado da operação.
	 */
	static boolean conferirEMigrar(EntityManager em, Usuario usuario, String senha) {
		String armazenado = usuario.getSenhaHash();

		if (armazenado == null || senha == null)
			return false;

		if (ehHash(armazenado))
			return confere(senha, armazenado);

		if (!armazenado.equals(senha))
			return false;

		try {
			em.getTransaction().begin();
			Usuario gerenciado = em.find(Usuario.class, usuario.getId());
			gerenciado.setSenhaHash(hash(senha));
			em.getTransaction().commit();
			usuario.setSenhaHash(gerenciado.getSenhaHash());
		}
		finally {
			if (em.getTransaction().isActive())
				em.getTransaction().rollback();
		}
		return true;
	}
	
	/**
	 * Deriva uma chave criptográfica a partir dos dados informados.
	 * 
	 * @param senha parâmetro utilizado na operação.
	 * @param sal parâmetro utilizado na operação.
	 * @param iteracoes parâmetro utilizado na operação.
	 * @return resultado da operação.
	 */
	private static byte[] derivar(String senha, byte[] sal, int iteracoes) {
		try {
			return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
					.generateSecret(new PBEKeySpec(senha.toCharArray(), sal, iteracoes, TAMANHO_HASH_BITS)).getEncoded();
		}
		catch (GeneralSecurityException e) {
			throw new IllegalStateException("Algoritmo de hash de senha indisponível.", e);
		}
	}
}
