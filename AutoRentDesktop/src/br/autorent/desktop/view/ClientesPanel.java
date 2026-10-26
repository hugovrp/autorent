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
import br.autorent.dto.ClienteDTO;
import br.autorent.sessionbeans.ICliente;

/** 
 * Tela de consulta e cadastro de clientes. 
 */
public class ClientesPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final java.awt.Frame janela;
	private final Callable<ICliente> servico;

	private final JTextField campoBusca = new JTextField(26);
	private final JButton botaoPesquisar = new JButton("Pesquisar");
	private final JButton botaoTodos = new JButton("Listar todos");
	private final JButton botaoNovo = new JButton("Novo cliente");
	private final JLabel mensagem = new JLabel(" ");
	private final ClientesTableModel modelo = new ClientesTableModel();

	private ICliente iCliente;

	public ClientesPanel(java.awt.Frame janela) {
		this(janela, ConexaoEJB::getClienteService);
	}

	public ClientesPanel(java.awt.Frame janela, Callable<ICliente> servico) {
		this.janela = janela;
		this.servico = servico;
		montarTela();

		botaoPesquisar.addActionListener(e -> pesquisar(campoBusca.getText()));
		campoBusca.addActionListener(e -> pesquisar(campoBusca.getText()));
		botaoTodos.addActionListener(e -> {
			campoBusca.setText("");
			pesquisar("");
		});
		botaoNovo.addActionListener(e -> novoCliente());
	}

	/** 
	 * Atualiza os dados quando a tela é exibida.
	 */
	public void aoExibir() {
		pesquisar(campoBusca.getText());
	}

	/** 
	 * Abre o formulário para cadastrar um cliente.
	 */
	public void novoCliente() {
		ClienteFormDialog dialogo = new ClienteFormDialog(janela, this::servico);
		dialogo.setVisible(true);

		ClienteDTO novo = dialogo.getCadastrado();
		if (novo != null) {
			campoBusca.setText("");
			pesquisar("", "Cliente \"" + novo.getNome() + "\" cadastrado com sucesso.");
		}
	}

	/** 
	 * Monta os componentes da interface.
	 */
	private void montarTela() {
		setLayout(new BorderLayout(0, 10));
		setBackground(Tema.FUNDO);
		setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));

		JLabel titulo = new JLabel("Clientes");
		titulo.setFont(Tema.TITULO.deriveFont(20f));

		JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		barra.setOpaque(false);
		barra.add(new JLabel("Nome, CPF ou e-mail:"));
		barra.add(campoBusca);
		barra.add(botaoPesquisar);
		barra.add(botaoTodos);
		barra.add(botaoNovo);

		JPanel topo = new JPanel(new BorderLayout(0, 12));
		topo.setOpaque(false);
		topo.add(titulo, BorderLayout.NORTH);
		topo.add(barra, BorderLayout.CENTER);

		JTable tabela = new JTable(modelo);
		tabela.setRowHeight(24);
		tabela.setFillsViewportHeight(true);
		tabela.getTableHeader().setReorderingAllowed(false);
		tabela.getColumnModel().getColumn(0).setMaxWidth(70);
		tabela.getColumnModel().getColumn(2).setPreferredWidth(130);

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
	private synchronized ICliente servico() throws Exception {
		if (iCliente == null) {
			iCliente = servico.call();
		}
		return iCliente;
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

		new SwingWorker<List<ClienteDTO>, Void>() {
			@Override
			/** 
			 * Executa a operação remota fora da thread da interface.
			 * 
			 * @return resultado da operação
			 */
			protected List<ClienteDTO> doInBackground() throws Exception {
				return servico().pesquisarClientes(texto);
			}

			@Override
			/** 
			 * Aplica o resultado da operação na interface.
			 */
			protected void done() {
				try {
					List<ClienteDTO> clientes = get();
					modelo.setClientes(clientes);
					if (mensagemSucesso != null) {
						mensagem(Tema.SUCESSO, mensagemSucesso);
					} else {
						mensagem(Tema.NEUTRO, clientes.isEmpty() ? "Nenhum cliente encontrado."
								: clientes.size() + " cliente(s) encontrado(s).");
					}
				} catch (Exception e) {
					synchronized (ClientesPanel.this) {
						iCliente = null; // refaz a referência na próxima tentativa
					}
					modelo.setClientes(new ArrayList<>());
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
		botaoTodos.setEnabled(habilitado);
		botaoNovo.setEnabled(habilitado);
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

	/** 
	 * Executa a operação de formatar cpf.
	 * 
	 * @param cpf valor utilizado pela operação
	 * @return resultado da operação
	 */
	private static String formatarCpf(String cpf) {
		if (cpf == null || cpf.length() != 11) {
			return cpf;
		}
		return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
	}

	private static class ClientesTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private static final String[] COLUNAS = { "Código", "Nome", "CPF", "E-mail" };

		private List<ClienteDTO> clientes = new ArrayList<>();

		void setClientes(List<ClienteDTO> clientes) {
			this.clientes = clientes;
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return clientes.size();
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
			ClienteDTO c = clientes.get(linha);
			switch (coluna) {
			case 0: return c.getId();
			case 1: return c.getNome();
			case 2: return formatarCpf(c.getCpf());
			default: return c.getEmail();
			}
		}
	}
}
