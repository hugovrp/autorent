package br.autorent.sessionbeans;

import java.time.LocalDate;
import java.util.List;

import br.autorent.dto.VeiculoDTO;
import br.autorent.modelo.Agencia;
import br.autorent.modelo.Veiculo;
import br.autorent.modelo.enums.CategoriaVeiculo;
import br.autorent.modelo.enums.StatusVeiculo;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;

/**
 * Serviço de cadastro, consulta e gerenciamento dos veículos.
 */
@Stateless
@Remote(IVeiculo.class)
public class VeiculoSB extends CrudBean<Veiculo, Long> implements IVeiculo {
	
    public VeiculoSB() { 
    	super(Veiculo.class); 
    }
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param agenciaId parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override 
    public List<Veiculo> buscarPorAgencia(Long agenciaId) {
        if (agenciaId == null) 
        	return List.of();
        
        return consultar("select v from Veiculo v where v.agencia.id = :valor order by v.modelo", "valor", agenciaId);
    }
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param status parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override 
    public List<Veiculo> buscarPorStatus(StatusVeiculo status) {
        if (status == null) 
        	return List.of();
        
        return consultar("select v from Veiculo v where v.status = :valor order by v.modelo", "valor", status);
    }
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param categoria parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override 
    public List<Veiculo> buscarPorCategoria(CategoriaVeiculo categoria) {
        if (categoria == null) 
        	return List.of();
        
        return consultar("select v from Veiculo v where v.categoria = :valor order by v.modelo", "valor", categoria);
    }
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param placa parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override 
    public Veiculo buscarPorPlaca(String placa) {
        if (placa == null || placa.isBlank()) 
        	return null;
        
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            List<Veiculo> lista = em.createQuery("select v from Veiculo v where upper(v.placa) = upper(:placa)", Veiculo.class)
                    .setParameter("placa", placa.trim()).setMaxResults(1).getResultList();
            
            return lista.isEmpty() ? null : lista.get(0);
        } 
        finally { 
        	em.close(); 
        }
    }
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param agenciaId parâmetro utilizado na operação.
     * @param categoria parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override 
    public List<Veiculo> buscarDisponiveis(Long agenciaId, CategoriaVeiculo categoria) {
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            String jpql = "select v from Veiculo v where v.status = :status";
            
            if (agenciaId != null) 
            	jpql += " and v.agencia.id = :agenciaId";
            
            if (categoria != null) 
            	jpql += " and v.categoria = :categoria";
            
            jpql += " order by v.modelo";
            var query = em.createQuery(jpql, Veiculo.class).setParameter("status", StatusVeiculo.DISPONIVEL);
            
            if (agenciaId != null) 
            	query.setParameter("agenciaId", agenciaId);
            
            if (categoria != null) 
            	query.setParameter("categoria", categoria);
            
            return query.getResultList();
        } 
        finally { 
        	em.close(); 
        }
    }
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param agenciaId parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override
    public List<Veiculo> buscarDisponiveisPorAgencia(Long agenciaId) {
        if (agenciaId == null)
        	return List.of();

        EntityManager em = JPAUtil.createEntityManager();

        try {
        	return em.createQuery("select v from Veiculo v join fetch v.agencia a "
        			+ "where a.id = :agenciaId and v.status = :status order by v.modelo", Veiculo.class)
        			.setParameter("agenciaId", agenciaId).setParameter("status", StatusVeiculo.DISPONIVEL)
        			.getResultList();
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
    public List<Veiculo> buscarAlugados() {
        EntityManager em = JPAUtil.createEntityManager();

        try {
        	return em.createQuery("select v from Veiculo v join fetch v.agencia "
        			+ "where v.status = :status order by v.modelo", Veiculo.class)
        			.setParameter("status", StatusVeiculo.LOCADO).getResultList();
        }
        finally {
        	em.close();
        }
    }
    
    /**
     * Valida os dados e cadastra um novo registro.
     * 
     * @param placa parâmetro utilizado na operação.
     * @param marca parâmetro utilizado na operação.
     * @param modelo parâmetro utilizado na operação.
     * @param ano parâmetro utilizado na operação.
     * @param categoria parâmetro utilizado na operação.
     * @param valorDiaria parâmetro utilizado na operação.
     * @param agenciaId parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override
    public VeiculoDTO cadastrarVeiculo(String placa, String marca, String modelo, Integer ano, CategoriaVeiculo categoria, Double valorDiaria, Long agenciaId) throws Exception {
        String placaNormalizada = placa == null ? "" : placa.replaceAll("[\\s-]", "").toUpperCase();
        
        if (!placaNormalizada.matches("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}"))
        	throw new Exception("Placa inválida. Use o formato ABC1234 ou ABC1D23.");

        if (marca == null || marca.isBlank())
        	throw new Exception("Informe a marca do veículo.");

        if (marca.trim().length() > 50)
        	throw new Exception("A marca deve ter no máximo 50 caracteres.");

        if (modelo == null || modelo.isBlank())
        	throw new Exception("Informe o modelo do veículo.");

        if (modelo.trim().length() > 80)
        	throw new Exception("O modelo deve ter no máximo 80 caracteres.");

        int anoMaximo = LocalDate.now().getYear() + 1;
        if (ano == null || ano < 1980 || ano > anoMaximo)
        	throw new Exception("Informe um ano entre 1980 e " + anoMaximo + ".");

        if (categoria == null)
        	throw new Exception("Selecione a categoria do veículo.");

        if (valorDiaria == null || valorDiaria <= 0)
        	throw new Exception("O valor da diária deve ser maior que zero.");

        if (agenciaId == null)
        	throw new Exception("Selecione a agência do veículo.");

        if (buscarPorPlaca(placaNormalizada) != null)
        	throw new Exception("Já existe um veículo cadastrado com esta placa.");

        EntityManager em = JPAUtil.createEntityManager();

        try {
        	em.getTransaction().begin();
        	Agencia agencia = em.find(Agencia.class, agenciaId);

        	if (agencia == null)
        		throw new Exception("Agência não encontrada.");

        	Veiculo veiculo = new Veiculo(placaNormalizada, marca.trim(), modelo.trim(), ano, categoria, valorDiaria, agencia);
        	em.persist(veiculo);
        	em.getTransaction().commit();

        	return new VeiculoDTO(veiculo.getId(), veiculo.getPlaca(), veiculo.getMarca(), veiculo.getModelo(), veiculo.getAno(),
        			veiculo.getCategoria(), veiculo.getValorDiaria(), veiculo.getStatus(), agencia.getId(), agencia.getNome(),
        			true, null);
        }
        finally {
        	if (em.getTransaction().isActive())
        		em.getTransaction().rollback();
        	
        	em.close();
        }
    }
    
    /**
     * Consulta informações relacionadas aos critérios informados.
     * 
     * @param jpql parâmetro utilizado na operação.
     * @param parametro parâmetro utilizado na operação.
     * @param valor parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    private List<Veiculo> consultar(String jpql, String parametro, Object valor) {
        EntityManager em = JPAUtil.createEntityManager();
        
        try { 
        	return em.createQuery(jpql, Veiculo.class).setParameter(parametro, valor).getResultList(); 
        }
        finally { 
        	em.close(); 
        }
    }
}
