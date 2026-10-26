package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.dto.UsuarioLogadoDTO;
import br.autorent.modelo.Usuario;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateful;
import jakarta.persistence.EntityManager;

/**
 * Gerencia autenticação, encerramento de sessão e dados do usuário autenticado.
 */
@Stateful
@Remote(IUsuario.class)
public class UsuarioSB implements IUsuario {
    private Usuario usuarioLogado;
    
    /**
     * Autentica o usuário com as credenciais informadas.
     * 
     * @param cpf parâmetro utilizado na operação.
     * @param senha parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override
    public boolean login(String cpf, String senha) {
        logout();
        
        if (cpf == null || cpf.isBlank() || senha == null) 
        	return false;
        
        String cpfNormalizado = cpf.replaceAll("\\D", "");
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            List<Usuario> usuarios = em.createQuery("select u from Usuario u where u.cpf = :cpf", Usuario.class)
                    .setParameter("cpf", cpfNormalizado).setMaxResults(1).getResultList();
            
            if (usuarios.isEmpty()) 
            	return false;
            
            Usuario usuario = usuarios.get(0);
            
            if (!SenhaUtil.conferirEMigrar(em, usuario, senha)) 
            	return false;
            
            usuarioLogado = usuario;
            return true;
        } 
        finally { 
        	em.close(); 
        }
    }
    
    /**
     * Encerra a sessão do usuário atual.
     */
    @Override 
    public void logout() { 
    	usuarioLogado = null; 
    }
    
    /**
     * Obtém as informações solicitadas.
     * 
     * @return resultado da operação.
     */
    @Override 
    public Usuario obterClienteLogado() { 
    	return usuarioLogado; 
    }
    
    /**
     * Obtém as informações solicitadas.
     * 
     * @return resultado da operação.
     */
    @Override
    public UsuarioLogadoDTO obterUsuarioLogado() {
        if (usuarioLogado == null)
        	return null;

        return new UsuarioLogadoDTO(usuarioLogado.getId(), usuarioLogado.getNome(), usuarioLogado.getTipo());
    }
}
