package br.autorent.sessionbeans;

import java.util.ArrayList;
import java.util.List;

import br.autorent.dto.AgenciaDTO;
import br.autorent.modelo.Agencia;
import br.autorent.modelo.enums.CategoriaVeiculo;
import br.autorent.modelo.enums.StatusVeiculo;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;

/**
 * Serviço de cadastro, consulta e pesquisa de agências.
 */
@Stateless
@Remote(IAgencia.class)
public class AgenciaSB extends CrudBean<Agencia, Long> implements IAgencia {
	
    public AgenciaSB() { 
    	super(Agencia.class); 
    }
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param cidade parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override
    public List<Agencia> buscarPorCidade(String cidade) {
        if (cidade == null || cidade.isBlank()) 
        	return List.of();
        
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            return em.createQuery("select a from Agencia a where lower(a.cidade) = lower(:cidade) order by a.nome", Agencia.class)
                    .setParameter("cidade", cidade.trim()).getResultList();
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
    public List<Agencia> buscarPorNome(String nome) {
        if (nome == null || nome.isBlank()) 
        	return List.of();
        
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            return em.createQuery("select a from Agencia a where lower(a.nome) like lower(:nome) order by a.nome", Agencia.class)
                    .setParameter("nome", "%" + nome.trim() + "%").getResultList();
        } 
        finally { 
        	em.close(); 
        }
    }
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param categoria parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override
    public List<Agencia> buscarVeiculosDisponiveis(CategoriaVeiculo categoria) {
        if (categoria == null)
        	return List.of();

        EntityManager em = JPAUtil.createEntityManager();

        try {
        	return em.createQuery("select distinct a from Agencia a join a.veiculos v "
        			+ "where v.status = :status and v.categoria = :categoria order by a.nome", Agencia.class)
        			.setParameter("status", StatusVeiculo.DISPONIVEL).setParameter("categoria", categoria)
        			.getResultList();
        }
        finally {
        	em.close();
        }
    }
    
    /**
     * Pesquisa registros e retorna os dados correspondentes.
     * 
     * @param texto parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override
    public List<AgenciaDTO> pesquisarAgencias(String texto) {
        EntityManager em = JPAUtil.createEntityManager();

        try {
        	List<Agencia> agencias;

        	if (texto == null || texto.isBlank())
        		agencias = em.createQuery("select a from Agencia a order by a.nome", Agencia.class).getResultList();
        	else
        		agencias = em.createQuery("select a from Agencia a where lower(a.nome) like :termo or lower(a.cidade) like :termo order by a.nome", Agencia.class)
        				.setParameter("termo", "%" + texto.trim().toLowerCase() + "%").getResultList();

        	List<AgenciaDTO> lista = new ArrayList<>();

        	for (Agencia a : agencias)
        		lista.add(new AgenciaDTO(a.getId(), a.getNome(), a.getCidade(), a.getEndereco(), a.getTelefone()));

        	return lista;
        }
        finally {
        	em.close();
        }
    }
    
    /**
     * Valida os dados e cadastra um novo registro.
     * 
     * @param nome parâmetro utilizado na operação.
     * @param endereco parâmetro utilizado na operação.
     * @param cidade parâmetro utilizado na operação.
     * @param telefone parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override
    public AgenciaDTO cadastrarAgencia(String nome, String endereco, String cidade, String telefone) throws Exception {
        if (nome == null || nome.isBlank())
        	throw new Exception("Informe o nome da agência.");

        if (nome.trim().length() > 100)
        	throw new Exception("O nome deve ter no máximo 100 caracteres.");

        if (endereco == null || endereco.isBlank())
        	throw new Exception("Informe o endereço da agência.");

        if (endereco.trim().length() > 200)
        	throw new Exception("O endereço deve ter no máximo 200 caracteres.");

        if (cidade == null || cidade.isBlank())
        	throw new Exception("Informe a cidade da agência.");

        if (cidade.trim().length() > 80)
        	throw new Exception("A cidade deve ter no máximo 80 caracteres.");

        String telefoneNormalizado = null;

        if (telefone != null && !telefone.isBlank()) {
        	if (!telefone.trim().matches("[0-9()\\s.\\-]+"))
        		throw new Exception("O telefone deve conter apenas números, parênteses, espaços, pontos e hífen.");

        	telefoneNormalizado = telefone.replaceAll("\\D", "");

        	if (telefoneNormalizado.length() != 10 && telefoneNormalizado.length() != 11)
        		throw new Exception("O telefone deve conter o DDD e 8 ou 9 dígitos.");
        }

        if (existe(nome.trim(), cidade.trim()))
        	throw new Exception("Já existe uma agência com este nome nesta cidade.");

        Agencia salva = criar(new Agencia(nome.trim(), endereco.trim(), cidade.trim(), telefoneNormalizado));
        return new AgenciaDTO(salva.getId(), salva.getNome(), salva.getCidade(), salva.getEndereco(), salva.getTelefone());
    }
    
    /**
     * Verifica se já existe um registro com os dados informados.
     * 
     * @param nome parâmetro utilizado na operação.
     * @param cidade parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    private boolean existe(String nome, String cidade) {
        EntityManager em = JPAUtil.createEntityManager();

        try {
            return em.createQuery("select count(a) from Agencia a where lower(a.nome) = lower(:nome) and lower(a.cidade) = lower(:cidade)", Long.class)
                    .setParameter("nome", nome).setParameter("cidade", cidade).getSingleResult() > 0;
        }
        finally {
        	em.close();
        }
    }
}
