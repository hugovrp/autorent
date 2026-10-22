package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.modelo.Usuario;
import br.autorent.modelo.enums.TipoUsuario;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;

@Stateless
@Remote(ICliente.class)
public class ClienteSB extends CrudBean<Usuario, Long> implements ICliente {
	
    public ClienteSB() { 
    	super(Usuario.class); 
    }

    @Override 
    public Usuario criar(Usuario cliente) {
        if (cliente == null) 
        	throw new IllegalArgumentException("O cliente não pode ser nulo.");
        
        cliente.setTipo(TipoUsuario.CLIENTE);
        return super.criar(cliente);
    }

    @Override 
    public Usuario atualizar(Usuario cliente) {
        if (cliente == null) 
        	throw new IllegalArgumentException("O cliente não pode ser nulo.");
        
        cliente.setTipo(TipoUsuario.CLIENTE);
        return super.atualizar(cliente);
    }

    @Override 
    public Usuario buscarPorCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) 
        	return null;
        
        String normalizado = cpf.replaceAll("\\D", "");
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            List<Usuario> lista = em.createQuery("select u from Usuario u where u.cpf = :cpf and u.tipo = :tipo", Usuario.class)
                    .setParameter("cpf", normalizado).setParameter("tipo", TipoUsuario.CLIENTE).setMaxResults(1).getResultList();
            
            return lista.isEmpty() ? null : lista.get(0);
        } 
        finally { 
        	em.close(); 
        }
    }
    
    @Override 
    public Usuario buscarPorEmail(String email) {
        if (email == null || email.isBlank()) 
        	return null;
        
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            List<Usuario> lista = em.createQuery("select u from Usuario u where lower(u.email) = lower(:email) and u.tipo = :tipo", Usuario.class)
                    .setParameter("email", email.trim()).setParameter("tipo", TipoUsuario.CLIENTE).setMaxResults(1).getResultList();
            return lista.isEmpty() ? null : lista.get(0);
        } 
        finally { 
        	em.close(); 
        }
    }
    
    @Override 
    public List<Usuario> buscarPorNome(String nome) {
        if (nome == null || nome.isBlank()) 
        	return List.of();
        
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            return em.createQuery("select u from Usuario u where lower(u.nome) like lower(:nome) and u.tipo = :tipo order by u.nome", Usuario.class)
                    .setParameter("nome", "%" + nome.trim() + "%").setParameter("tipo", TipoUsuario.CLIENTE).getResultList();
        }
        finally { 
        	em.close(); 
        }
    }
    
    @Override 
    public List<Usuario> buscarTodos() {
        EntityManager em = JPAUtil.createEntityManager();
        
        try { 
        	return em.createQuery("select u from Usuario u where u.tipo = :tipo order by u.nome", Usuario.class)
                .setParameter("tipo", TipoUsuario.CLIENTE).getResultList(); 
        }
        finally { 
        	em.close(); 
        }
    }
}

