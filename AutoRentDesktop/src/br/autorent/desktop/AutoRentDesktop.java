package br.autorent.desktop;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import br.autorent.desktop.view.LoginFrame;

/** 
 * Inicializa a aplicação desktop utilizada pelos funcionários da locadora. 
 */
public class AutoRentDesktop {

	/** 
	 * Inicia a aplicação desktop.
	 * 
	 * @param args valor utilizado pela operação
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			aplicarLookAndFeel();
			new LoginFrame().setVisible(true);
		});
	}

	/** 
	 * Executa a operação de aplicar look and feel.
	 */
	private static void aplicarLookAndFeel() {
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					return;
				}
			}
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (Exception e) {
			// mantém o visual padrão do Swing
		}
	}
}