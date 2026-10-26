package br.autorent.desktop.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import br.autorent.desktop.Mensagens;
import br.autorent.dto.AgenciaDTO;
import br.autorent.dto.ClienteDTO;
import br.autorent.dto.LocacaoDTO;
import br.autorent.dto.VeiculoDTO;
import br.autorent.modelo.enums.StatusVeiculo;
import br.autorent.sessionbeans.ICliente;
import br.autorent.sessionbeans.IConsultaFrota;
import br.autorent.sessionbeans.IOperacaoLocacao;

/** 
 * Formulário para registrar uma locação de veículo. 
 */
public class LocacaoDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
			.withResolverStyle(ResolverStyle.STRICT);

	private final Callable<ICliente> servicoCliente;
	private final Callable<IConsultaFrota> servicoConsulta;
	private final Callable<IOperacaoLocacao> servicoOperacao;

	private final JTextField campoBuscaCliente = new JTextField(16);
	private final JButton botaoBuscarCliente = new JButton("Buscar");
	private final JComboBox<ClienteDTO> comboCliente = new JComboBox<>();
	private final SeletorAgencia comboRetirada = new SeletorAgencia(false);
	private final SeletorAgencia comboDevolucao = new SeletorAgencia(false);
	private final JComboBox<VeiculoDTO> comboVeiculo = new JComboBox<>();
	private final JTextField campoRetirada = new JTextField(10);
	private final JTextField campoDevolucao = new JTextField(10);
	private final JTextArea mensagem = new JTextArea(3, 24);
	private final JButton botaoSalvar = new JButton("Registrar locação");
	private final JButton botaoCancelar = new JButton("Cancelar");

	private IOperacaoLocacao operacao;
	private boolean ajustando;
	private boolean devolucaoSegueRetirada = true;
	private LocacaoDTO cadastrada;

	public LocacaoDialog(Frame dono, Callable<ICliente> servicoCliente, Callable<IConsultaFrota> servicoConsulta,
			Callable<IOperacaoLocacao> servicoOperacao) {
		super(dono, "Nova locação", true);
		this.servicoCliente = servicoCliente;
		this.servicoConsulta = servicoConsulta;
		this.servicoOperacao = servicoOperacao;

		configurarRenderers();
		montarTela();

		LocalDate hoje = LocalDate.now();
		campoRetirada.setText(hoje.format(FORMATO_DATA));
		campoDevolucao.setText(hoje.plusDays(1).format(FORMATO_DATA));

		botaoBuscarCliente.addActionListener(e -> carregarClientes(campoBuscaCliente.getText(), false));
		campoBuscaCliente.addActionListener(e -> carregarClientes(campoBuscaCliente.getText(), false));
		comboRetirada.addActionListener(e -> {
			if (!ajustando) {
				if (devolucaoSegueRetirada) {
					ajustando = true;
					comboDevolucao.selecionar(comboRetirada.getAgenciaId());
					ajustando = false;
				}
				carregarVeiculos();
			}
		});
		comboDevolucao.addActionListener(e -> {
			if (!ajustando) {
				devolucaoSegueRetirada = false;
			}
		});
		botaoSalvar.addActionListener(e -> salvar());
		botaoCancelar.addActionListener(e -> dispose());
		getRootPane().setDefaultButton(botaoSalvar);
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			/** 
			 * Executa a operação de window closed.
			 * 
			 * @param e valor utilizado pela operação
			 */
			public void windowClosed(java.awt.event.WindowEvent e) {
				encerrarSessao();
			}
		});

		pack();
		setResizable(false);
		setLocationRelativeTo(dono);
		carregarInicial();
	}

	public LocacaoDTO getCadastrada() {
		return cadastrada;
	}

	/** 
	 * Configura a apresentação dos itens nas listas.
	 */
	private void configurarRenderers() {
		comboCliente.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object valor, int indice, boolean selecionado,
					boolean foco) {
				ClienteDTO c = (ClienteDTO) valor;
				return super.getListCellRendererComponent(list,
						c == null ? "" : c.getNome() + " - CPF " + Formatos.cpf(c.getCpf()), indice, selecionado, foco);
			}
		});
		comboVeiculo.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object valor, int indice, boolean selecionado,
					boolean foco) {
				VeiculoDTO v = (VeiculoDTO) valor;
				return super.getListCellRendererComponent(list,
						v == null ? "" : v.getMarca() + " " + v.getModelo() + " (" + v.getPlaca() + ") - "
								+ Formatos.categoria(v.getCategoria()) + " - " + Formatos.moeda(v.getValorDiaria()) + "/dia",
						indice, selecionado, foco);
			}
		});
	}

	/** 
	 * Monta os componentes da interface.
	 */
	private void montarTela() {
		JPanel form = new JPanel(new GridBagLayout());
		form.setBorder(BorderFactory.createEmptyBorder(18, 24, 8, 24));
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;

		JPanel buscaCliente = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		buscaCliente.add(campoBuscaCliente);
		buscaCliente.add(botaoBuscarCliente);
		campo(form, c, "Cliente (busque por nome, CPF ou e-mail)", buscaCliente);
		campo(form, c, "Cliente selecionado", comboCliente);
		campo(form, c, "Agência de retirada", comboRetirada);
		campo(form, c, "Veículo disponível nessa agência", comboVeiculo);
		campo(form, c, "Agência de devolução", comboDevolucao);

		JPanel datas = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		datas.add(new JLabel("Retirada:"));
		datas.add(campoRetirada);
		datas.add(new JLabel("Devolução:"));
		datas.add(campoDevolucao);
		campo(form, c, "Período (dd/mm/aaaa)", datas);

		mensagem.setEditable(false);
		mensagem.setFocusable(false);
		mensagem.setOpaque(false);
		mensagem.setLineWrap(true);
		mensagem.setWrapStyleWord(true);
		mensagem.setFont(Tema.ROTULO);
		c.insets = new Insets(10, 0, 0, 0);
		form.add(mensagem, c);

		JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		botoes.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));
		botoes.add(botaoSalvar);
		botoes.add(botaoCancelar);

		setLayout(new BorderLayout());
		add(form, BorderLayout.CENTER);
		add(botoes, BorderLayout.SOUTH);
	}

	/** 
	 * Adiciona um campo identificado à área do formulário.
	 * 
	 * @param painel valor utilizado pela operação
	 * @param c valor utilizado pela operação
	 * @param titulo valor utilizado pela operação
	 * @param entrada valor utilizado pela operação
	 */
	private void campo(JPanel painel, GridBagConstraints c, String titulo, Component entrada) {
		JLabel rotulo = new JLabel(titulo);
		rotulo.setFont(Tema.ROTULO);
		c.insets = new Insets(8, 0, 2, 0);
		painel.add(rotulo, c);
		c.insets = new Insets(0, 0, 0, 0);
		painel.add(entrada, c);
	}

	/** 
	 * Carrega os dados necessários para iniciar o formulário.
	 */
	private void carregarInicial() {
		habilitar(false);
		mensagem(Tema.NEUTRO, "Carregando dados...");

		new SwingWorker<Object[], Void>() {
			@Override
			/** 
			 * Executa a operação remota fora da thread da interface.
			 * 
			 * @return resultado da operação
			 */
			protected Object[] doInBackground() throws Exception {
				return new Object[] { servicoConsulta.call().listarAgencias(), servicoCliente.call().pesquisarClientes("") };
			}

			@Override
			@SuppressWarnings("unchecked")
			/** 
			 * Aplica o resultado da operação na interface.
			 */
			protected void done() {
				try {
					Object[] resultado = get();
					List<AgenciaDTO> agencias = (List<AgenciaDTO>) resultado[0];
					ajustando = true;
					comboRetirada.carregar(agencias);
					comboDevolucao.carregar(agencias);
					ajustando = false;
					preencherClientes((List<ClienteDTO>) resultado[1]);
					if (agencias.isEmpty()) {
						mensagem(Tema.ERRO, "Nenhuma agência cadastrada. Cadastre uma agência antes de registrar locações.");
						habilitar(false);
						botaoCancelar.setEnabled(true);
					} else {
						habilitar(true);
						carregarVeiculos();
					}
				} catch (Exception e) {
					ajustando = false;
					mensagem(Tema.ERRO, Mensagens.de(e.getCause() != null ? e.getCause() : e));
					botaoCancelar.setEnabled(true);
				}
			}
		}.execute();
	}

	/** 
	 * Carrega os clientes disponíveis para a operação.
	 * 
	 * @param texto valor utilizado pela operação
	 * @param silencioso valor utilizado pela operação
	 */
	private void carregarClientes(String texto, boolean silencioso) {
		habilitar(false);
		mensagem(Tema.NEUTRO, "Buscando clientes...");

		new SwingWorker<List<ClienteDTO>, Void>() {
			@Override
			/** 
			 * Executa a operação remota fora da thread da interface.
			 * 
			 * @return resultado da operação
			 */
			protected List<ClienteDTO> doInBackground() throws Exception {
				return servicoCliente.call().pesquisarClientes(texto);
			}

			@Override
			/** 
			 * Aplica o resultado da operação na interface.
			 */
			protected void done() {
				try {
					preencherClientes(get());
				} catch (Exception e) {
					mensagem(Tema.ERRO, Mensagens.de(e.getCause() != null ? e.getCause() : e));
				}
				habilitar(true);
			}
		}.execute();
	}

	/** 
	 * Preenche a lista com os clientes recebidos.
	 * 
	 * @param clientes valor utilizado pela operação
	 */
	private void preencherClientes(List<ClienteDTO> clientes) {
		comboCliente.setModel(new DefaultComboBoxModel<>(clientes.toArray(new ClienteDTO[0])));
		mensagem(Tema.NEUTRO, clientes.isEmpty() ? "Nenhum cliente encontrado."
				: clientes.size() + " cliente(s) encontrado(s). Selecione o cliente da locação.");
	}

	/** 
	 * Carrega os veículos disponíveis para a locação.
	 */
	private void carregarVeiculos() {
		Long agenciaId = comboRetirada.getAgenciaId();
		habilitar(false);
		mensagem(Tema.NEUTRO, "Consultando veículos disponíveis...");

		new SwingWorker<List<VeiculoDTO>, Void>() {
			@Override
			/** 
			 * Executa a operação remota fora da thread da interface.
			 * 
			 * @return resultado da operação
			 */
			protected List<VeiculoDTO> doInBackground() throws Exception {
				return servicoConsulta.call().consultarFrota(agenciaId, StatusVeiculo.DISPONIVEL);
			}

			@Override
			/** 
			 * Aplica o resultado da operação na interface.
			 */
			protected void done() {
				try {
					List<VeiculoDTO> veiculos = new ArrayList<>(get());
					comboVeiculo.setModel(new DefaultComboBoxModel<>(veiculos.toArray(new VeiculoDTO[0])));
					mensagem(Tema.NEUTRO, veiculos.isEmpty() ? "Nenhum veículo disponível nesta agência."
							: veiculos.size() + " veículo(s) disponível(is) nesta agência.");
				} catch (Exception e) {
					comboVeiculo.setModel(new DefaultComboBoxModel<>());
					mensagem(Tema.ERRO, Mensagens.de(e.getCause() != null ? e.getCause() : e));
				}
				habilitar(true);
			}
		}.execute();
	}

	/** 
	 * Valida os dados e inicia a operação de gravação.
	 */
	private void salvar() {
		ClienteDTO cliente = (ClienteDTO) comboCliente.getSelectedItem();
		VeiculoDTO veiculo = (VeiculoDTO) comboVeiculo.getSelectedItem();

		if (cliente == null) {
			erro("Selecione o cliente.", campoBuscaCliente);
			return;
		}
		if (comboRetirada.getAgenciaId() == null) {
			erro("Selecione a agência de retirada.", comboRetirada);
			return;
		}
		if (veiculo == null) {
			erro("Selecione o veículo.", comboVeiculo);
			return;
		}
		if (comboDevolucao.getAgenciaId() == null) {
			erro("Selecione a agência de devolução.", comboDevolucao);
			return;
		}

		LocalDate retirada = lerData(campoRetirada, "retirada");
		if (retirada == null) {
			return;
		}
		LocalDate devolucao = lerData(campoDevolucao, "devolução");
		if (devolucao == null) {
			return;
		}

		preparar(cliente, veiculo, retirada, devolucao);
	}

	/** 
	 * Lê e valida a data informada no campo.
	 * 
	 * @param campo valor utilizado pela operação
	 * @param nome valor utilizado pela operação
	 * @return resultado da operação
	 */
	private LocalDate lerData(JTextField campo, String nome) {
		try {
			return LocalDate.parse(campo.getText().trim(), FORMATO_DATA);
		} catch (Exception e) {
			erro("Informe a data de " + nome + " no formato dd/mm/aaaa.", campo);
			return null;
		}
	}

	/** 
	 * Prepara a locação e calcula seu valor junto ao backend.
	 * 
	 * @param cliente valor utilizado pela operação
	 * @param veiculo valor utilizado pela operação
	 * @param retirada valor utilizado pela operação
	 * @param devolucao valor utilizado pela operação
	 */
	private void preparar(ClienteDTO cliente, VeiculoDTO veiculo, LocalDate retirada, LocalDate devolucao) {
		Long agenciaRetirada = comboRetirada.getAgenciaId();
		Long agenciaDevolucao = comboDevolucao.getAgenciaId();
		AgenciaDTO nomeRetirada = (AgenciaDTO) comboRetirada.getSelectedItem();
		AgenciaDTO nomeDevolucao = (AgenciaDTO) comboDevolucao.getSelectedItem();

		habilitar(false);
		mensagem(Tema.NEUTRO, "Validando...");

		new SwingWorker<Double, Void>() {
			@Override
			/** 
			 * Executa a operação remota fora da thread da interface.
			 * 
			 * @return resultado da operação
			 */
			protected Double doInBackground() throws Exception {
				return operacao().prepararLocacaoAtendimento(cliente.getId(), agenciaRetirada, agenciaDevolucao,
						veiculo.getId(), retirada, devolucao);
			}

			@Override
			/** 
			 * Aplica o resultado da operação na interface.
			 */
			protected void done() {
				try {
					double total = get();
					habilitar(true);
					mensagem(Tema.NEUTRO, "Confirme os dados da locação.");
					String resumo = "<html>Cliente: <b>" + cliente.getNome() + "</b><br>Veículo: <b>" + veiculo.getMarca() + " "
							+ veiculo.getModelo() + " (" + veiculo.getPlaca() + ")</b><br>Retirada: <b>" + Formatos.data(retirada)
							+ "</b> em " + nomeRetirada.getNome() + "<br>Devolução: <b>" + Formatos.data(devolucao)
							+ "</b> em " + nomeDevolucao.getNome() + "<br><br>Valor total: <b>" + Formatos.moeda(total)
							+ "</b><br><br>Confirmar a locação?</html>";
					int opcao = JOptionPane.showConfirmDialog(LocacaoDialog.this, resumo, "Confirmar locação",
							JOptionPane.YES_NO_OPTION);
					if (opcao == JOptionPane.YES_OPTION) {
						confirmar();
					} else {
						mensagem(Tema.NEUTRO, "Locação não confirmada. Ajuste os dados ou cancele.");
					}
				} catch (Exception e) {
					falhar(e);
				}
			}
		}.execute();
	}

	/** 
	 * Confirma a locação preparada.
	 */
	private void confirmar() {
		habilitar(false);
		mensagem(Tema.NEUTRO, "Salvando...");

		new SwingWorker<LocacaoDTO, Void>() {
			@Override
			/** 
			 * Executa a operação remota fora da thread da interface.
			 * 
			 * @return resultado da operação
			 */
			protected LocacaoDTO doInBackground() throws Exception {
				return operacao().confirmarLocacaoAtendimento();
			}

			@Override
			/** 
			 * Aplica o resultado da operação na interface.
			 */
			protected void done() {
				try {
					cadastrada = get();
					dispose();
				} catch (Exception e) {
					falhar(e);
				}
			}
		}.execute();
	}

	/** 
	 * Obtém o serviço de operações de locação.
	 * 
	 * @return resultado da operação
	 */
	private synchronized IOperacaoLocacao operacao() throws Exception {
		if (operacao == null) {
			operacao = servicoOperacao.call(); // bean stateful: a referência deve ser guardada
		}
		return operacao;
	}

	/** 
	 * Trata uma falha ocorrida durante a operação.
	 * 
	 * @param e valor utilizado pela operação
	 */
	private void falhar(Exception e) {
		Throwable causa = e.getCause() != null ? e.getCause() : e;
		if (causa.getClass() != Exception.class) {
			synchronized (this) {
				operacao = null;
			}
		}
		mensagem(Tema.ERRO, Mensagens.de(causa));
		habilitar(true);
	}

	/** 
	 * Executa a operação de encerrar sessao.
	 */
	private void encerrarSessao() {
		IOperacaoLocacao sessao;
		synchronized (this) {
			sessao = operacao;
			operacao = null;
		}
		if (sessao == null) {
			return;
		}
		new SwingWorker<Void, Void>() {
			@Override
			/** 
			 * Executa a operação remota fora da thread da interface.
			 * 
			 * @return resultado da operação
			 */
			protected Void doInBackground() {
				try {
					sessao.encerrar();
				} catch (Exception e) {
					// a sessão do servidor expira sozinha
				}
				return null;
			}
		}.execute();
	}

	/** 
	 * Define se os controles da interface ficam habilitados.
	 * 
	 * @param habilitado valor utilizado pela operação
	 */
	private void habilitar(boolean habilitado) {
		for (Component c : new Component[] { campoBuscaCliente, botaoBuscarCliente, comboCliente, comboRetirada,
				comboVeiculo, comboDevolucao, campoRetirada, campoDevolucao, botaoSalvar, botaoCancelar }) {
			c.setEnabled(habilitado);
		}
	}

	/** 
	 * Exibe uma mensagem de erro e direciona o foco para o campo indicado.
	 * 
	 * @param texto valor utilizado pela operação
	 * @param foco valor utilizado pela operação
	 */
	private void erro(String texto, Component foco) {
		mensagem(Tema.ERRO, texto);
		foco.requestFocusInWindow();
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

	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		return new Dimension(Math.max(d.width, 520), d.height);
	}
}
