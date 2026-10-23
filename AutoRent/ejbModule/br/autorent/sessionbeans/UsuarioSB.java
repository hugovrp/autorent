package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.modelo.Usuario;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateful;
import jakarta.persistence.EntityManager;

@Stateful
@Remote(IUsuario.class)
public class UsuarioSB implements IUsuario {
    private Usuario usuarioLogado;

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
            
            if (usuario.getSenhaHash() == null || !usuario.getSenhaHash().equals(senha)) 
            	return false;
            
            usuarioLogado = usuario;
            return true;
        } 
        finally { 
        	em.close(); 
        }
    }

    @Override 
    public void logout() { 
    	usuarioLogado = null; 
    }
    
    @Override 
    public Usuario obterClienteLogado() { 
    	return usuarioLogado; 
    }
}

