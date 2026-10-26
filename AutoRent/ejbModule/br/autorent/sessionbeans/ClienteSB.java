package br.autorent.sessionbeans;

import java.util.ArrayList;
import java.util.List;

import br.autorent.dto.ClienteDTO;
import br.autorent.modelo.Usuario;
import br.autorent.modelo.enums.TipoUsuario;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;

/**
 * Serviço de cadastro e gerenciamento dos clientes.
 */
@Stateless
@Remote(ICliente.class)
public class ClienteSB extends CrudBean<Usuario, Long> implements ICliente {
	
    public ClienteSB() { 
    	super(Usuario.class); 
    }
    
    /**
     * Cria e persiste um novo registro.
     * 
     * @param cliente parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override 
    public Usuario criar(Usuario cliente) {
        if (cliente == null) 
        	throw new IllegalArgumentException("O cliente não pode ser nulo.");
        
        cliente.setTipo(TipoUsuario.CLIENTE);
        protegerSenha(cliente);
        return super.criar(cliente);
    }
    
    /**
     * Atualiza os dados de um registro existente.
     * 
     * @param cliente parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override 
    public Usuario atualizar(Usuario cliente) {
        if (cliente == null) 
        	throw new IllegalArgumentException("O cliente não pode ser nulo.");
        
        cliente.setTipo(TipoUsuario.CLIENTE);
        protegerSenha(cliente);
        return super.atualizar(cliente);
    }
    
    /**
     * Protege os dados sensíveis antes de armazená-los.
     * 
     * @param usuario parâmetro utilizado na operação.
     */
    private void protegerSenha(Usuario usuario) {
        if (usuario.getSenhaHash() != null && !SenhaUtil.ehHash(usuario.getSenhaHash()))
        	usuario.setSenhaHash(SenhaUtil.hash(usuario.getSenhaHash()));
    }
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param cpf parâmetro utilizado na operação.
     * @return resultado da operação.
     */
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
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param email parâmetro utilizado na operação.
     * @return resultado da operação.
     */
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
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param nome parâmetro utilizado na operação.
     * @return resultado da operação.
     */
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
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @return resultado da operação.
     */
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
    
    /**
     * Lista os registros disponíveis.
     * 
     * @return resultado da operação.
     */
    @Override
    public List<ClienteDTO> listarClientes() {
        return dto(buscarTodos());
    }
    
    /**
     * Pesquisa registros e retorna os dados correspondentes.
     * 
     * @param texto parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override
    public List<ClienteDTO> pesquisarClientes(String texto) {
        if (texto == null || texto.isBlank())
        	return listarClientes();

        String termo = "%" + texto.trim().toLowerCase() + "%";
        String digitos = texto.replaceAll("\\D", "");
        EntityManager em = JPAUtil.createEntityManager();

        try {
            String jpql = "select u from Usuario u where u.tipo = :tipo and (lower(u.nome) like :termo or lower(u.email) like :termo"
                    + (digitos.isEmpty() ? "" : " or u.cpf like :cpf") + ") order by u.nome";
            var consulta = em.createQuery(jpql, Usuario.class).setParameter("tipo", TipoUsuario.CLIENTE).setParameter("termo", termo);

            if (!digitos.isEmpty())
            	consulta.setParameter("cpf", "%" + digitos + "%");

            return dto(consulta.getResultList());
        }
        finally {
        	em.close();
        }
    }
    
    /**
     * Valida os dados e cadastra um novo registro.
     * 
     * @param nome parâmetro utilizado na operação.
     * @param cpf parâmetro utilizado na operação.
     * @param email parâmetro utilizado na operação.
     * @param senha parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override
    public ClienteDTO cadastrarCliente(String nome, String cpf, String email, String senha) throws Exception {
        if (nome == null || nome.isBlank())
        	throw new Exception("Informe o nome do cliente.");

        if (nome.trim().length() > 120)
        	throw new Exception("O nome deve ter no máximo 120 caracteres.");

        String cpfNormalizado = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (cpfNormalizado.length() != 11)
        	throw new Exception("O CPF deve conter 11 dígitos.");

        if (email == null || !email.trim().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+") || email.trim().length() > 150)
        	throw new Exception("Informe um e-mail válido.");

        if (senha == null || senha.length() < 6)
        	throw new Exception("A senha deve ter no mínimo 6 caracteres.");

        if (existe("cpf", cpfNormalizado))
        	throw new Exception("Já existe um usuário cadastrado com este CPF.");

        if (existe("email", email.trim().toLowerCase()))
        	throw new Exception("Já existe um usuário cadastrado com este e-mail.");

        Usuario salvo = criar(new Usuario(nome.trim(), cpfNormalizado, email.trim(), senha, TipoUsuario.CLIENTE));
        return new ClienteDTO(salvo.getId(), salvo.getNome(), salvo.getCpf(), salvo.getEmail());
    }
    
    /**
     * Verifica se já existe um registro com os dados informados.
     * 
     * @param campo parâmetro utilizado na operação.
     * @param valor parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    private boolean existe(String campo, String valor) {
        EntityManager em = JPAUtil.createEntityManager();

        try {
            String jpql = "select count(u) from Usuario u where " + ("cpf".equals(campo) ? "u.cpf = :valor" : "lower(u.email) = :valor");
            return em.createQuery(jpql, Long.class).setParameter("valor", valor).getSingleResult() > 0;
        }
        finally {
        	em.close();
        }
    }
    
    /**
     * Executa a operação correspondente aos dados informados.
     * 
     * @param usuarios parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    private List<ClienteDTO> dto(List<Usuario> usuarios) {
        List<ClienteDTO> lista = new ArrayList<>();

        for (Usuario u : usuarios)
        	lista.add(new ClienteDTO(u.getId(), u.getNome(), u.getCpf(), u.getEmail()));

        return lista;
    }
}
