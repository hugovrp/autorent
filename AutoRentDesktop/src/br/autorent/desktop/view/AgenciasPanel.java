package br.autorent.desktop.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

import br.autorent.desktop.ConexaoEJB;
import br.autorent.desktop.Mensagens;
import br.autorent.dto.AgenciaDTO;
import br.autorent.sessionbeans.IAgencia;

/** 
 * Tela de consulta e cadastro de agências. 
 */
public class AgenciasPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final java.awt.Frame janela;
	private final Callable<IAgencia> servico;

	private final JTextField campoBusca = new JTextField(26);
	private final JButton botaoPesquisar = new JButton("Pesquisar");
	private final JButton botaoTodas = new JButton("Listar todas");
	private final JButton botaoNova = new JButton("Nova agência");
	private final JLabel mensagem = new JLabel(" ");
	private final AgenciasTableModel modelo = new AgenciasTableModel();

	private IAgencia iAgencia;

	public AgenciasPanel(java.awt.Frame janela) {
		this(janela, ConexaoEJB::getAgenciaService);
	}

	public AgenciasPanel(java.awt.Frame janela, Callable<IAgencia> servico) {
		this.janela = janela;
		this.servico = servico;
		montarTela();

		botaoPesquisar.addActionListener(e -> pesquisar(campoBusca.getText()));
		campoBusca.addActionListener(e -> pesquisar(campoBusca.getText()));
		botaoTodas.addActionListener(e -> {
			campoBusca.setText("");
			pesquisar("");
		});
		botaoNova.addActionListener(e -> novaAgencia());
	}

	/** 
	 * Atualiza os dados quando a tela é exibida.
	 */
	public void aoExibir() {
		pesquisar(campoBusca.getText());
	}

	/** 
	 * Abre o formulário para cadastrar uma agência.
	 */
	public void novaAgencia() {
		AgenciaFormDialog dialogo = new AgenciaFormDialog(janela, this::servico);
		dialogo.setVisible(true);

		AgenciaDTO nova = dialogo.getCadastrada();
		if (nova != null) {
			campoBusca.setText("");
			pesquisar("", "Agência \"" + nova.getNome() + "\" (" + nova.getCidade() + ") cadastrada com sucesso.");
		}
	}

	/** 
	 * Monta os componentes da interface.
	 */
	private void montarTela() {
		setLayout(new BorderLayout(0, 10));
		setBackground(Tema.FUNDO);
		setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));

		JLabel titulo = new JLabel("Agências");
		titulo.setFont(Tema.TITULO.deriveFont(20f));

		JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		barra.setOpaque(false);
		barra.add(new JLabel("Nome ou cidade:"));
		barra.add(campoBusca);
		barra.add(botaoPesquisar);
		barra.add(botaoTodas);
		barra.add(botaoNova);

		JPanel topo = new JPanel(new BorderLayout(0, 12));
		topo.setOpaque(false);
		topo.add(titulo, BorderLayout.NORTH);
		topo.add(barra, BorderLayout.CENTER);

		JTable tabela = new JTable(modelo);
		tabela.setRowHeight(24);
		tabela.setFillsViewportHeight(true);
		tabela.getTableHeader().setReorderingAllowed(false);
		tabela.getColumnModel().getColumn(0).setMaxWidth(70);
		tabela.getColumnModel().getColumn(3).setPreferredWidth(220);
		tabela.getColumnModel().getColumn(4).setPreferredWidth(110);

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
	private synchronized IAgencia servico() throws Exception {
		if (iAgencia == null) {
			iAgencia = servico.call();
		}
		return iAgencia;
	}

	/** 
	 * Consulta os registros usando o texto informado.
	 * 
	 * @param texto valor utilizado pela operação
	 */
	private void pesquisar(String texto) {
		pesquisar(texto, null);
	}

	/** 
	 * Consulta os registros usando o texto informado.
	 * 
	 * @param texto valor utilizado pela operação
	 * @param mensagemSucesso valor utilizado pela operação
	 */
	private void pesquisar(String texto, String mensagemSucesso) {
		habilitar(false);
		mensagem(Tema.NEUTRO, "Consultando...");

		new SwingWorker<List<AgenciaDTO>, Void>() {
			@Override
			/** 
			 * Executa a operação remota fora da thread da interface.
			 * 
			 * @return resultado da operação
			 */
			protected List<AgenciaDTO> doInBackground() throws Exception {
				return servico().pesquisarAgencias(texto);
			}

			@Override
			/** 
			 * Aplica o resultado da operação na interface.
			 */
			protected void done() {
				try {
					List<AgenciaDTO> agencias = get();
					modelo.setAgencias(agencias);
					if (mensagemSucesso != null) {
						mensagem(Tema.SUCESSO, mensagemSucesso);
					} 
					else {
						mensagem(Tema.NEUTRO, agencias.isEmpty() ? "Nenhuma agência encontrada."
								: agencias.size() + " agência(s) encontrada(s).");
					}
				} 
				catch (Exception e) {
					synchronized (AgenciasPanel.this) {
						iAgencia = null; // refaz a referência na próxima tentativa
					}
					modelo.setAgencias(new ArrayList<>());
					mensagem(Tema.ERRO, Mensagens.de(e.getCause() != null ? e.getCause() : e));
				}
				habilitar(true);
			}
		}.execute();
	}

	/** 
	 * Define se os controles da interface ficam habilitados.
	 * 
	 * @param habilitado valor utilizado pela operação
	 */
	private void habilitar(boolean habilitado) {
		botaoPesquisar.setEnabled(habilitado);
		botaoTodas.setEnabled(habilitado);
		botaoNova.setEnabled(habilitado);
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

	private static class AgenciasTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private static final String[] COLUNAS = { "Código", "Nome", "Cidade", "Endereço", "Telefone" };

		private List<AgenciaDTO> agencias = new ArrayList<>();

		void setAgencias(List<AgenciaDTO> agencias) {
			this.agencias = agencias;
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return agencias.size();
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
			AgenciaDTO a = agencias.get(linha);
			switch (coluna) {
			case 0: return a.getId();
			case 1: return a.getNome();
			case 2: return a.getCidade();
			case 3: return a.getEndereco();
			default: return Formatos.telefone(a.getTelefone());
			}
		}
	}
}
