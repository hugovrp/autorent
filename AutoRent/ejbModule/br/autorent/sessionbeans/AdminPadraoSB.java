package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.modelo.Usuario;
import br.autorent.modelo.enums.TipoUsuario;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;

/**
 * Inicializa a aplicação garantindo a existência de uma conta administrativa padrão.
 */
@Singleton
@Startup
public class AdminPadraoSB {
	public static final String CPF_PADRAO = "00000000000";
	public static final String SENHA_PADRAO = "admin";

	/**
	 * Garante a configuração necessária para a aplicação.
	 */
	@PostConstruct
	void garantirAdministrador() {
		EntityManager em = JPAUtil.createEntityManager();

		try {
			List<Usuario> existentes = em.createQuery("select u from Usuario u where u.cpf = :cpf or u.email = :email", Usuario.class)
					.setParameter("cpf", CPF_PADRAO).setParameter("email", "admin@autorent.com").setMaxResults(1).getResultList();

			if (!existentes.isEmpty())
				return;

			em.getTransaction().begin();
			em.persist(new Usuario("Administrador", CPF_PADRAO, "admin@autorent.com", SenhaUtil.hash(SENHA_PADRAO), TipoUsuario.FUNCIONARIO));
			em.getTransaction().commit();
		}
		catch (RuntimeException e) {
			e.printStackTrace();
		}
		finally {
			if (em.getTransaction().isActive())
				em.getTransaction().rollback();
			em.close();
		}
	}
}
