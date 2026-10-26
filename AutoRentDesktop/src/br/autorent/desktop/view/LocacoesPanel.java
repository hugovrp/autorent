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
import br.autorent.dto.LocacaoDTO;
import br.autorent.modelo.enums.StatusLocacao;
import br.autorent.sessionbeans.ICliente;
import br.autorent.sessionbeans.IConsultaFrota;
import br.autorent.sessionbeans.IOperacaoLocacao;

/** 
 * Tela de consulta e gerenciamento das locações. 
 */
public class LocacoesPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final int COLUNA_SITUACAO = 8;

	private final Callable<IConsultaFrota> servico;
	private final SeletorAgencia comboAgencia = new SeletorAgencia();
	private final JComboBox<String> comboTipo = new JComboBox<>(
			new String[] { "Em andamento (ativas)", "Reservas (aguardando retirada)" });
	private final JButton botaoConsultar = new JButton("Consultar");
	private final JButton botaoNova = new JButton("Nova locação");
	private final JLabel mensagem = new JLabel(" ");
	private final LocacoesTableModel modelo = new LocacoesTableModel();

	private final java.awt.Frame janela;
	private final Callable<ICliente> servicoCliente;
	private final Callable<IOperacaoLocacao> servicoOperacao;
	private IConsultaFrota iConsulta;

	public LocacoesPanel() {
		this((java.awt.Frame) null);
	}

	public LocacoesPanel(java.awt.Frame janela) {
		this(janela, ConexaoEJB::getConsultaFrotaService, ConexaoEJB::getClienteService,
				ConexaoEJB::getOperacaoLocacaoService);
	}

	public LocacoesPanel(Callable<IConsultaFrota> servico) {
		this(null, servico, ConexaoEJB::getClienteService, ConexaoEJB::getOperacaoLocacaoService);
	}

	public LocacoesPanel(java.awt.Frame janela, Callable<IConsultaFrota> servico, Callable<ICliente> servicoCliente,
			Callable<IOperacaoLocacao> servicoOperacao) {
		this.janela = janela;
		this.servico = servico;
		this.servicoCliente = servicoCliente;
		this.servicoOperacao = servicoOperacao;
		montarTela();
		botaoConsultar.addActionListener(e -> consultar());
		botaoNova.addActionListener(e -> novaLocacao());
	}

	/** 
	 * Executa a operação de nova locacao.
	 */
	public void novaLocacao() {
		LocacaoDialog dialogo = new LocacaoDialog(janela, servicoCliente, servico, servicoOperacao);
		dialogo.setVisible(true);

		LocacaoDTO nova = dialogo.getCadastrada();
		if (nova != null) {
			// a locação nasce como reserva: mostra todas as reservas, independente dos filtros anteriores
			comboTipo.setSelectedIndex(1);
			comboAgencia.setSelectedIndex(0);
			consultar("Locação nº " + nova.getId() + " registrada com sucesso para " + nova.getClienteNome() + ": "
					+ nova.getVeiculoMarca() + " " + nova.getVeiculoModelo() + " (" + nova.getVeiculoPlaca() + "), de "
					+ Formatos.data(nova.getDataRetirada()) + " a " + Formatos.data(nova.getDataPrevDevolucao())
					+ ", total " + Formatos.moeda(nova.getValorTotal()) + ". Situação: reservada, aguardando retirada.");
		}
	}

	/** 
	 * Atualiza os dados quando a tela é exibida.
	 */
	public void aoExibir() {
		consultar();
	}

	/** 
	 * Monta os componentes da interface.
	 */
	private void montarTela() {
		setLayout(new BorderLayout(0, 10));
		setBackground(Tema.FUNDO);
		setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));

		JLabel titulo = new JLabel("Locações");
		titulo.setFont(Tema.TITULO.deriveFont(20f));

		JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		filtros.setOpaque(false);
		filtros.add(new JLabel("Exibir:"));
		filtros.add(comboTipo);
		filtros.add(new JLabel("Agência:"));
		filtros.add(comboAgencia);
		filtros.add(botaoConsultar);
		filtros.add(botaoNova);

		JPanel topo = new JPanel(new BorderLayout(0, 12));
		topo.setOpaque(false);
		topo.add(titulo, BorderLayout.NORTH);
		topo.add(filtros, BorderLayout.CENTER);

		JTable tabela = new JTable(modelo);
		tabela.setRowHeight(24);
		tabela.setFillsViewportHeight(true);
		tabela.getTableHeader().setReorderingAllowed(false);
		tabela.getColumnModel().getColumn(0).setMaxWidth(50);
		tabela.getColumnModel().getColumn(3).setPreferredWidth(260);
		tabela.getColumnModel().getColumn(COLUNA_SITUACAO).setCellRenderer(new SituacaoRenderer());

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
		StatusLocacao status = comboTipo.getSelectedIndex() == 0 ? StatusLocacao.ATIVA : StatusLocacao.RESERVADA;
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
				return new Object[] { s.listarAgencias(), s.consultarLocacoes(status, agenciaId) };
			}

			@Override
			@SuppressWarnings("unchecked")
			/** 
			 * Aplica o resultado da operação na interface.
			 */
			protected void done() {
				try {
					Object[] resultado = get();
					comboAgencia.carregar((List<AgenciaDTO>) resultado[0]);
					List<LocacaoDTO> locacoes = (List<LocacaoDTO>) resultado[1];
					modelo.setLocacoes(locacoes);
					long atrasadas = locacoes.stream().filter(LocacaoDTO::isAtrasada).count();
					if (mensagemSucesso != null) {
						mensagem(Tema.SUCESSO, mensagemSucesso);
					} else {
						mensagem(atrasadas > 0 ? new Color(0xB45309) : Tema.NEUTRO, locacoes.isEmpty()
								? "Nenhuma locação encontrada."
								: locacoes.size() + " locação(ões) encontrada(s)"
										+ (atrasadas > 0 ? ", " + atrasadas + " com devolução atrasada." : "."));
					}
				} catch (Exception e) {
					synchronized (LocacoesPanel.this) {
						iConsulta = null; // refaz a referência na próxima tentativa
					}
					modelo.setLocacoes(new ArrayList<>());
					mensagem(Tema.ERRO, Mensagens.de(e.getCause() != null ? e.getCause() : e));
				}
				botaoConsultar.setEnabled(true);
			}
		}.execute();
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
			if (!selecionada) {
				c.setForeground(modelo.get(linha).isAtrasada() ? Tema.ERRO : Tema.SUCESSO);
			}
			return c;
		}
	}

	private static class LocacoesTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private static final String[] COLUNAS = { "Nº", "Cliente", "CPF", "Veículo", "Retirada", "Devolução prevista",
				"Retirar em", "Devolver em", "Situação", "Valor" };

		private List<LocacaoDTO> locacoes = new ArrayList<>();

		void setLocacoes(List<LocacaoDTO> locacoes) {
			this.locacoes = locacoes;
			fireTableDataChanged();
		}

		LocacaoDTO get(int linha) {
			return locacoes.get(linha);
		}

		@Override
		public int getRowCount() {
			return locacoes.size();
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
			LocacaoDTO l = locacoes.get(linha);
			switch (coluna) {
				case 0: return l.getId();
				case 1: return l.getClienteNome();
				case 2: return Formatos.cpf(l.getClienteCpf());
				case 3: return l.getVeiculoMarca() + " " + l.getVeiculoModelo() + " (" + l.getVeiculoPlaca() + ")";
				case 4: return Formatos.data(l.getDataRetirada());
				case 5: return Formatos.data(l.getDataPrevDevolucao());
				case 6: return l.getAgenciaRetiradaNome();
				case 7: return l.getAgenciaDevolucaoNome();
				case 8: return l.isAtrasada() ? "Atrasada" : Formatos.statusLocacao(l.getStatus());
				default: return Formatos.moeda(l.getValorTotal());
			}
		}
	}
}
