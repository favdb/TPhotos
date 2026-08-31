/*
 * Copyright (C) 2026 favdb
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package api.sherpa;

import app.App;
import static app.App.pref;
import app.Pref;
import app.i18n.I18N;
import app.resources.icons.IconUtil;
import app.tools.LaF;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * class to test SHERPA
 *
 * @author favdb
 */
public class SherpaTest extends JDialog {

	private SHERPA editor;

	public SherpaTest(JFrame parentFrame) {
		super(parentFrame, "SHERPA Component test", true);
		initComponent();
	}

	private void initComponent() {
		setLayout(new BorderLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(800, 600));
		// Initialization od SHERPA editor
		editor = new SHERPA();
		add(editor, BorderLayout.CENTER);
		// Actions panel for testing
		initBottomPanel();
		// Automatic load of the exemple.html file
		loadSampleFromFileFile();
		pack();
		setLocationRelativeTo(getOwner());
	}

	private void initBottomPanel() {
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton printConsoleButton = new JButton("Show HTML (Console)");
		printConsoleButton.addActionListener(e -> {
			System.out.println("=== HTML product by SHE ===");
			System.out.println(editor.htmlContentGet());
			System.out.println("============================");
		});
		JButton reloadButton = new JButton("Reload sample");
		reloadButton.addActionListener(e -> loadSampleFromFileFile());
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(e -> dispose());
		bottomPanel.add(reloadButton);
		bottomPanel.add(printConsoleButton);
		bottomPanel.add(closeButton);
		add(bottomPanel, BorderLayout.SOUTH);
	}

	private void loadSampleFromFileFile() {
		try (InputStream inputStream = getClass().getResourceAsStream("exemple.html")) {
			if (inputStream == null) {
				JOptionPane.showMessageDialog(this,
						"Unable to find 'exemple.html' fila in the package.",
						"Loading error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream, StandardCharsets.UTF_8))) {
				String htmlContent = reader.lines().collect(Collectors.joining("\n"));
				editor.htmlContentSet(htmlContent);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					"Error when reading file : " + e.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void main(String[] args) {
		pref = new Pref();
		I18N.initMessages(Locale.getDefault());
		App.fontInit();
		LaF.init();
		IconUtil.setDefSize();
		SwingUtilities.invokeLater(() -> {
			SherpaTest testDialog = new SherpaTest(null);
			testDialog.setVisible(true);
			System.exit(0);
		});
	}

}
