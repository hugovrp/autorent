package br.autorent.desktop.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import br.autorent.desktop.ConexaoEJB;
import br.autorent.desktop.Mensagens;
import br.autorent.dto.AgenciaDTO;
import br.autorent.dto.VeiculoDTO;
import br.autorent.modelo.enums.StatusVeiculo;
import br.autorent.sessionbeans.IConsultaFrota;
import br.autorent.sessionbeans.IVeiculo;

/** 
 * Tela de consulta e gerenciamento da frota de veículos. 
 */
public class FrotaPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final int COLUNA_SITUACAO = 6;

	private final Callable<IConsultaFrota> servico;
	private final SeletorAgencia comboAgencia = new SeletorAgencia();
	private final JComboBox<String> comboSituacao = new JComboBox<>(
			new String[] { "Todas", "Disponível", "Locado", "Em manutenção" });
	private final JButton botaoConsultar = new JButton("Consultar");
	private final JButton botaoNovo = new JButton("Novo veículo");
	private final JLabel mensagem = new JLabel(" ");
	private final FrotaTableModel modelo = new FrotaTableModel();

	private final java.awt.Frame janela;
	private List<AgenciaDTO> agencias = new ArrayList<>();
	private IConsultaFrota iConsulta;

	private final Callable<IVeiculo> servicoVeiculo;

	public FrotaPanel(java.awt.Frame janela) {
		this(janela, ConexaoEJB::getConsultaFrotaService, ConexaoEJB::getVeiculoService);
	}

	public FrotaPanel(java.awt.Frame janela, Callable<IConsultaFrota> servico, Callable<IVeiculo> servicoVeiculo) {
		this.janela = janela;
		this.servico = servico;
		this.servicoVeiculo = servicoVeiculo;
		montarTela();
		botaoConsultar.addActionListener(e -> consultar());
		botaoNovo.addActionListener(e -> novoVeiculo());
	}

	/** 
	 * Atualiza os dados quando a tela é exibida.
	 */
	public void aoExibir() {
		consultar();
	}

	/** 
	 * Inicia o cadastro de um veículo.
	 */
	public void novoVeiculo() {
		botaoNovo.setEnabled(false);
		new SwingWorker<List<AgenciaDTO>, Void>() {
			@Override
			/** 
			 * Executa a operação remota fora da thread da interface.
			 * 
			 * @return resultado da operação
			 */
			protected List<AgenciaDTO> doInBackground() throws Exception {
				return servico().listarAgencias();
			}

			@Override
			/** 
			 * Aplica o resultado da operação na interface.
			 */
			protected void done() {
				botaoNovo.setEnabled(true);
				try {
					agencias = get();
				} catch (Exception e) {
					synchronized (FrotaPanel.this) {
						iConsulta = null;
					}
					mensagem(Tema.ERRO, Mensagens.de(e.getCause() != null ? e.getCause() : e));
					return;
				}
				abrirFormulario();
			}
		}.execute();
	}

	/** 
	 * Executa a operação de abrir formulario.
	 */
	private void abrirFormulario() {
		VeiculoFormDialog dialogo = new VeiculoFormDialog(janela, servicoVeiculo, agencias);
		dialogo.setVisible(true);

		VeiculoDTO novo = dialogo.getCadastrado();
		if (novo != null) {
			comboAgencia.setSelectedIndex(0);
			comboSituacao.setSelectedIndex(0);
			consultar("Veículo " + novo.getPlaca() + " (" + novo.getMarca() + " " + novo.getModelo()
					+ ") cadastrado com sucesso.");
		}
	}

	/** 
	 * Monta os componentes da interface.
	 */
	private void montarTela() {
		setLayout(new BorderLayout(0, 10));
		setBackground(Tema.FUNDO);
		setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));

		JLabel titulo = new JLabel("Frota");
		titulo.setFont(Tema.TITULO.deriveFont(20f));

		JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		filtros.setOpaque(false);
		filtros.add(new JLabel("Agência:"));
		filtros.add(comboAgencia);
		filtros.add(new JLabel("Situação:"));
		filtros.add(comboSituacao);
		filtros.add(botaoConsultar);
		filtros.add(botaoNovo);

		JPanel topo = new JPanel(new BorderLayout(0, 12));
		topo.setOpaque(false);
		topo.add(titulo, BorderLayout.NORTH);
		topo.add(filtros, BorderLayout.CENTER);

		JTable tabela = new JTable(modelo);
		tabela.setRowHeight(24);
		tabela.setFillsViewportHeight(true);
		tabela.getTableHeader().setReorderingAllowed(false);
		tabela.getColumnModel().getColumn(COLUNA_SITUACAO).setCellRenderer(new SituacaoRenderer());
		tabela.getColumnModel().getColumn(2).setMaxWidth(60);
		tabela.getColumnModel().getColumn(7).setPreferredWidth(150);

		mensagem.setFont(Tema.ROTULO);

		add(topo, BorderLayout.NORTH);
		add(new JScrollPane(tabela), BorderLayout.CENTER);
		add(mensagem, BorderLayout.SOUTH);
	}

	/** 
	 * Executa a operação de servico.
	 * 
	 * @return resultado da operação
	 */
	private synchronized IConsultaFrota servico() throws Exception {
		if (iConsulta == null) {
			iConsulta = servico.call();
		}
		return iConsulta;
	}

	/** 
	 * Executa a operação de situacao escolhida.
	 * 
	 * @return resultado da operação
	 */
	private StatusVeiculo situacaoEscolhida() {
		switch (comboSituacao.getSelectedIndex()) {
		case 1: return StatusVeiculo.DISPONIVEL;
		case 2: return StatusVeiculo.LOCADO;
		case 3: return StatusVeiculo.MANUTENCAO;
		default: return null;
		}
	}

	/** 
	 * Consulta os registros que atendem aos critérios atuais.
	 */
	private void consultar() {
		consultar(null);
	}

	/** 
	 * Consulta os registros que atendem aos critérios atuais.
	 * 
	 * @param mensagemSucesso valor utilizado pela operação
	 */
	private void consultar(String mensagemSucesso) {
		Long agenciaId = comboAgencia.getAgenciaId();
		StatusVeiculo status = situacaoEscolhida();
		botaoConsultar.setEnabled(false);
		mensagem(Tema.NEUTRO, "Consultando...");

		new SwingWorker<Object[], Void>() {
			@Override
			/** 
			 * Executa a operação remota fora da thread da interface.
			 * 
			 * @return resultado da operação
			 */
			protected Object[] doInBackground() throws Exception {
				IConsultaFrota s = servico();
				return new Object[] { s.listarAgencias(), s.consultarFrota(agenciaId, status) };
			}

			@Override
			@SuppressWarnings("unchecked")
			/** 
			 * Aplica o resultado da operação na interface.
			 */
			protected void done() {
				try {
					Object[] resultado = get();
					agencias = (List<AgenciaDTO>) resultado[0];
					comboAgencia.carregar(agencias);
					List<VeiculoDTO> frota = (List<VeiculoDTO>) resultado[1];
					modelo.setFrota(frota);
					if (mensagemSucesso != null) {
						mensagem(Tema.SUCESSO, mensagemSucesso);
					} else {
						mensagem(Tema.NEUTRO, resumo(frota));
					}
				} 
				catch (Exception e) {
					synchronized (FrotaPanel.this) {
						iConsulta = null; // refaz a referência na próxima tentativa
					}
					modelo.setFrota(new ArrayList<>());
					mensagem(Tema.ERRO, Mensagens.de(e.getCause() != null ? e.getCause() : e));
				}
				botaoConsultar.setEnabled(true);
			}
		}.execute();
	}

	/** 
	 * Gera um resumo dos veículos da frota.
	 * 
	 * @param frota valor utilizado pela operação
	 * @return resultado da operação
	 */
	private String resumo(List<VeiculoDTO> frota) {
		if (frota.isEmpty()) {
			return "Nenhum veículo encontrado.";
		}
		long disponiveis = frota.stream().filter(VeiculoDTO::isDisponivelParaLocacao).count();
		long locados = frota.stream().filter(v -> v.getStatus() == StatusVeiculo.LOCADO).count();
		long manutencao = frota.stream().filter(v -> v.getStatus() == StatusVeiculo.MANUTENCAO).count();
		return frota.size() + " veículo(s): " + disponiveis + " disponível(is) para locação, " + locados + " locado(s), "
				+ manutencao + " em manutenção.";
	}

	/** 
	 * Exibe uma mensagem com a cor informada.
	 * 
	 * @param cor valor utilizado pela operação
	 * @param texto valor utilizado pela operação
	 */
	private void mensagem(Color cor, String texto) {
		mensagem.setForeground(cor);
		mensagem.setText(texto);
	}

	private class SituacaoRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada, boolean foco,
				int linha, int coluna) {
			Component c = super.getTableCellRendererComponent(tabela, valor, selecionada, foco, linha, coluna);
			VeiculoDTO v = modelo.get(linha);
			if (!selecionada) {
				c.setForeground(v.getStatus() == StatusVeiculo.DISPONIVEL ? Tema.SUCESSO
						: v.getStatus() == StatusVeiculo.LOCADO ? new Color(0xB45309) : Tema.NEUTRO);
			}
			return c;
		}
	}

	private static class FrotaTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private static final String[] COLUNAS = { "Placa", "Veículo", "Ano", "Categoria", "Diária", "Agência", "Situação",
				"Disponível p/ locação", "Devolução prevista" };

		private List<VeiculoDTO> frota = new ArrayList<>();

		void setFrota(List<VeiculoDTO> frota) {
			this.frota = frota;
			fireTableDataChanged();
		}

		VeiculoDTO get(int linha) {
			return frota.get(linha);
		}

		@Override
		public int getRowCount() {
			return frota.size();
		}

		@Override
		public int getColumnCount() {
			return COLUNAS.length;
		}

		@Override
		public String getColumnName(int coluna) {
			return COLUNAS[coluna];
		}

		@Override
		public Object getValueAt(int linha, int coluna) {
			VeiculoDTO v = frota.get(linha);
			switch (coluna) {
			case 0: return v.getPlaca();
			case 1: return v.getMarca() + " " + v.getModelo();
			case 2: return v.getAno();
			case 3: return Formatos.categoria(v.getCategoria());
			case 4: return Formatos.moeda(v.getValorDiaria());
			case 5: return v.getAgenciaNome();
			case 6: return Formatos.statusVeiculo(v.getStatus());
			case 7: return v.isDisponivelParaLocacao() ? "Sim" : "Não";
			default: return Formatos.data(v.getDevolucaoPrevista());
			}
		}
	}
}
