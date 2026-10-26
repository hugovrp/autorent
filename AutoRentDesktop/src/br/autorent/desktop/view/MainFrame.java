package br.autorent.desktop.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import br.autorent.desktop.ConexaoEJB;
import br.autorent.desktop.SessaoFuncionario;
import br.autorent.dto.UsuarioLogadoDTO;

/** 
 * Janela principal e navegação entre os módulos da aplicação. 
 */
public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	public static final String TELA_INICIO = "inicio";
	public static final String TELA_CLIENTES = "clientes";
	public static final String TELA_AGENCIAS = "agencias";
	public static final String TELA_RETIRADA = "retirada";
	public static final String TELA_DEVOLUCAO = "devolucao";
	public static final String TELA_FROTA = "frota";
	public static final String TELA_LOCACOES = "locacoes";

	private final CardLayout cartoes = new CardLayout();
	private final JPanel conteudo = new JPanel(cartoes);
	private final JLabel barraStatus = new JLabel();
	private ClientesPanel telaClientes;
	private AgenciasPanel telaAgencias;
	private FrotaPanel telaFrota;
	private LocacoesPanel telaLocacoes;
	private RetiradaPanel telaRetirada;
	private DevolucaoPanel telaDevolucao;

	public MainFrame(UsuarioLogadoDTO funcionario) {
		super("AutoRent - Atendimento");
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		setJMenuBar(criarMenu());
		setLayout(new BorderLayout());
		add(criarCabecalho(funcionario), BorderLayout.NORTH);

		conteudo.setBackground(Tema.FUNDO);
		adicionarTela(TELA_INICIO, criarTelaInicial(funcionario));
		telaClientes = new ClientesPanel(this);
		adicionarTela(TELA_CLIENTES, telaClientes);
		telaAgencias = new AgenciasPanel(this);
		adicionarTela(TELA_AGENCIAS, telaAgencias);
		telaFrota = new FrotaPanel(this);
		adicionarTela(TELA_FROTA, telaFrota);
		telaLocacoes = new LocacoesPanel(this);
		adicionarTela(TELA_LOCACOES, telaLocacoes);
		telaRetirada = new RetiradaPanel();
		adicionarTela(TELA_RETIRADA, telaRetirada);
		telaDevolucao = new DevolucaoPanel();
		adicionarTela(TELA_DEVOLUCAO, telaDevolucao);
		add(conteudo, BorderLayout.CENTER);

		barraStatus.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
		mensagemStatus("Login realizado com sucesso. Conectado a " + ConexaoEJB.getEnderecoServidor() + ".");
		add(barraStatus, BorderLayout.SOUTH);

		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			/** 
			 * Executa a operação de window closing.
			 * 
			 * @param e valor utilizado pela operação
			 */
			public void windowClosing(java.awt.event.WindowEvent e) {
				confirmarSaida();
			}
		});

		setSize(900, 600);
		setMinimumSize(new Dimension(720, 480));
		setLocationRelativeTo(null);
	}

	/** 
	 * Registra uma tela na janela principal.
	 * 
	 * @param id valor utilizado pela operação
	 * @param tela valor utilizado pela operação
	 */
	public void adicionarTela(String id, JComponent tela) {
		conteudo.add(tela, id);
	}

	/** 
	 * Exibe a tela identificada.
	 * 
	 * @param id valor utilizado pela operação
	 */
	public void mostrarTela(String id) {
		cartoes.show(conteudo, id);
	}

	/** 
	 * Exibe uma mensagem de status.
	 * 
	 * @param texto valor utilizado pela operação
	 */
	public void mensagemStatus(String texto) {
		barraStatus.setForeground(Tema.SUCESSO);
		barraStatus.setText(texto);
	}

	/** 
	 * Exibe uma mensagem de erro na área de status.
	 * 
	 * @param texto valor utilizado pela operação
	 */
	public void erroStatus(String texto) {
		barraStatus.setForeground(Tema.ERRO);
		barraStatus.setText(texto);
	}

	/** 
	 * Monta a barra de menus da aplicação.
	 * 
	 * @return resultado da operação
	 */
	private JMenuBar criarMenu() {
		JMenuBar barra = new JMenuBar();

		JMenu arquivo = new JMenu("Arquivo");
		JMenuItem trocar = new JMenuItem("Trocar usuário");
		trocar.addActionListener(e -> trocarUsuario());
		JMenuItem sair = new JMenuItem("Sair");
		sair.addActionListener(e -> confirmarSaida());
		arquivo.add(trocar);
		arquivo.addSeparator();
		arquivo.add(sair);
		barra.add(arquivo);

		barra.add(criarMenuClientes());
		barra.add(criarMenuAgencias());
		barra.add(criarMenuFrota());
		barra.add(criarMenuLocacoes());
		return barra;
	}

	/** 
	 * Cria o menu de operações de clientes.
	 * 
	 * @return resultado da operação
	 */
	private JMenu criarMenuClientes() {
		JMenu menu = new JMenu("Clientes");
		JMenuItem consulta = new JMenuItem("Consulta de clientes");
		consulta.addActionListener(e -> abrirClientes());
		JMenuItem novo = new JMenuItem("Novo cliente");
		novo.addActionListener(e -> {
			abrirClientes();
			telaClientes.novoCliente();
		});
		menu.add(consulta);
		menu.add(novo);
		return menu;
	}

	/** 
	 * Cria o menu de operações de agências.
	 * 
	 * @return resultado da operação
	 */
	private JMenu criarMenuAgencias() {
		JMenu menu = new JMenu("Agências");
		JMenuItem consulta = new JMenuItem("Consulta de agências");
		consulta.addActionListener(e -> abrirAgencias());
		JMenuItem nova = new JMenuItem("Nova agência");
		nova.addActionListener(e -> {
			abrirAgencias();
			telaAgencias.novaAgencia();
		});
		menu.add(consulta);
		menu.add(nova);
		return menu;
	}

	/** 
	 * Cria o menu de operações da frota.
	 * 
	 * @return resultado da operação
	 */
	private JMenu criarMenuFrota() {
		JMenu menu = new JMenu("Frota");
		JMenuItem consulta = new JMenuItem("Consulta da frota");
		consulta.addActionListener(e -> abrirFrota());
		JMenuItem novo = new JMenuItem("Novo veículo");
		novo.addActionListener(e -> {
			abrirFrota();
			telaFrota.novoVeiculo();
		});
		menu.add(consulta);
		menu.add(novo);
		return menu;
	}

	/**
	 * Cria o menu de operações de locações.
	 * 
	 * @return resultado da operação
	 */
	private JMenu criarMenuLocacoes() {
		JMenu menu = new JMenu("Locações");
		JMenuItem andamento = new JMenuItem("Locações em andamento");
		andamento.addActionListener(e -> abrirLocacoes());
		menu.add(andamento);
		menu.addSeparator();
		JMenuItem nova = new JMenuItem("Nova locação");
		nova.addActionListener(e -> {
			abrirLocacoes();
			telaLocacoes.novaLocacao();
		});
		menu.add(nova);
		JMenuItem retirada = new JMenuItem("Retirada");
		retirada.addActionListener(e -> abrirRetirada());
		menu.add(retirada);
		JMenuItem devolucao = new JMenuItem("Devolução");
		devolucao.addActionListener(e -> abrirDevolucao());
		menu.add(devolucao);
		return menu;
	}

	/** 
	 * Exibe a tela de agências.
	 */
	private void abrirAgencias() {
		mostrarTela(TELA_AGENCIAS);
		telaAgencias.aoExibir();
	}

	/** 
	 * Exibe a tela da frota.
	 */
	private void abrirFrota() {
		mostrarTela(TELA_FROTA);
		telaFrota.aoExibir();
	}

	/** 
	 * Exibe a tela de locações.
	 */
	private void abrirLocacoes() {
		mostrarTela(TELA_LOCACOES);
		telaLocacoes.aoExibir();
	}

	/** 
	 * Exibe a tela de devolução de veículos.
	 */
	private void abrirDevolucao() {
		mostrarTela(TELA_DEVOLUCAO);
		telaDevolucao.aoExibir();
	}

	/** 
	 * Exibe a tela de retirada de veículos.
	 */
	private void abrirRetirada() {
		mostrarTela(TELA_RETIRADA);
		telaRetirada.aoExibir();
	}

	/** 
	 * Exibe a tela de clientes.
	 */
	private void abrirClientes() {
		mostrarTela(TELA_CLIENTES);
		telaClientes.aoExibir();
	}

	/** 
	 * Monta o cabeçalho com os dados do funcionário.
	 * 
	 * @param funcionario valor utilizado pela operação
	 * @return resultado da operação
	 */
	private JPanel criarCabecalho(UsuarioLogadoDTO funcionario) {
		JPanel cabecalho = new JPanel(new BorderLayout());
		cabecalho.setBackground(Tema.AZUL);
		cabecalho.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

		JLabel marca = new JLabel("AutoRent");
		marca.setFont(Tema.TITULO);
		marca.setForeground(Color.WHITE);

		JLabel usuario = new JLabel(funcionario.getNome() + "  |  Funcionário", SwingConstants.RIGHT);
		usuario.setForeground(Color.WHITE);

		cabecalho.add(marca, BorderLayout.WEST);
		cabecalho.add(usuario, BorderLayout.EAST);
		return cabecalho;
	}

	/** 
	 * Monta a tela inicial da aplicação.
	 * 
	 * @param funcionario valor utilizado pela operação
	 * @return resultado da operação
	 */
	private JPanel criarTelaInicial(UsuarioLogadoDTO funcionario) {
		JPanel tela = new JPanel(new BorderLayout());
		tela.setBackground(Tema.FUNDO);
		tela.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

		JLabel boasVindas = new JLabel("Bem-vindo, " + funcionario.getNome() + "!");
		boasVindas.setFont(Tema.TITULO.deriveFont(20f));
		tela.add(boasVindas, BorderLayout.NORTH);

		JPanel modulos = new JPanel(new GridLayout(1, 4, 16, 0));
		modulos.setOpaque(false);
		modulos.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));
		modulos.add(cartaoComBotao("Clientes", "Cadastro e consulta de clientes", "Abrir", this::abrirClientes));
		modulos.add(cartaoComBotao("Agências", "Cadastro e consulta de agências", "Abrir", this::abrirAgencias));
		modulos.add(cartaoComBotao("Frota", "Consulta, situação, disponibilidade e cadastro de veículos", "Abrir", this::abrirFrota));
		modulos.add(cartaoComBotao("Locações", "Consulta, nova locação, retirada e devolução", "Abrir", this::abrirLocacoes));

		JPanel centro = new JPanel(new BorderLayout());
		centro.setOpaque(false);
		centro.add(modulos, BorderLayout.NORTH);
		tela.add(centro, BorderLayout.CENTER);
		return tela;
	}

	/** 
	 * Cria um cartão com botão para executar uma ação.
	 * 
	 * @param titulo valor utilizado pela operação
	 * @param descricao valor utilizado pela operação
	 * @param textoBotao valor utilizado pela operação
	 * @param acao valor utilizado pela operação
	 * @return resultado da operação
	 */
	private JPanel cartaoComBotao(String titulo, String descricao, String textoBotao, Runnable acao) {
		JPanel cartao = cartaoModulo(titulo, descricao);
		JButton botao = new JButton(textoBotao);
		botao.addActionListener(e -> acao.run());
		cartao.remove(((BorderLayout) cartao.getLayout()).getLayoutComponent(BorderLayout.SOUTH));
		JPanel rodape = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
		rodape.setOpaque(false);
		rodape.add(botao);
		cartao.add(rodape, BorderLayout.SOUTH);
		return cartao;
	}

	/** 
	 * Cria um cartão informativo de módulo.
	 * 
	 * @param titulo valor utilizado pela operação
	 * @param descricao valor utilizado pela operação
	 * @return resultado da operação
	 */
	private JPanel cartaoModulo(String titulo, String descricao) {
		JPanel cartao = new JPanel(new BorderLayout(0, 6));
		cartao.setBackground(Color.WHITE);
		cartao.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(0xD1D5DB)),
				BorderFactory.createEmptyBorder(16, 16, 16, 16)));

		JLabel nome = new JLabel(titulo);
		nome.setFont(Tema.ROTULO.deriveFont(15f));
		JLabel texto = new JLabel("<html>" + descricao + "</html>");
		JLabel emBreve = new JLabel("Em breve");
		emBreve.setForeground(Tema.NEUTRO);

		cartao.add(nome, BorderLayout.NORTH);
		cartao.add(texto, BorderLayout.CENTER);
		cartao.add(emBreve, BorderLayout.SOUTH);
		return cartao;
	}

	/** 
	 * Encerra a sessão atual e retorna ao login.
	 */
	private void trocarUsuario() {
		encerrarSessao(() -> {
			new LoginFrame().setVisible(true);
			dispose();
		});
	}

	/** 
	 * Solicita confirmação antes de sair da aplicação.
	 */
	private void confirmarSaida() {
		int opcao = JOptionPane.showConfirmDialog(this, "Deseja encerrar a sessão e sair do sistema?", "Sair",
				JOptionPane.YES_NO_OPTION);
		if (opcao == JOptionPane.YES_OPTION) {
			encerrarSessao(() -> System.exit(0));
		}
	}

	/** 
	 * Executa a operação de encerrar sessao.
	 * 
	 * @param depois valor utilizado pela operação
	 */
	private void encerrarSessao(Runnable depois) {
		barraStatus.setForeground(Tema.NEUTRO);
		barraStatus.setText("Encerrando sessão...");
		new SwingWorker<Void, Void>() {
			@Override
			/** 
			 * Executa a operação remota fora da thread da interface.
			 * 
			 * @return resultado da operação
			 */
			protected Void doInBackground() {
				SessaoFuncionario.getInstancia().sair();
				return null;
			}

			@Override
			/** 
			 * Aplica o resultado da operação na interface.
			 */
			protected void done() {
				depois.run();
			}
		}.execute();
	}
}
