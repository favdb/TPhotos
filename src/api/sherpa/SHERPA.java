/*
 * Copyright (C) 2024-2026 favdb
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
import app.i18n.I18N;
import app.resources.icons.ICONS;
import app.resources.icons.IconButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

public class SHERPA extends JPanel {

	private static final String TT = "SHERPA.";

	private JEditorPane editorPanePane;
	private JToolBar toolBar;
	private JComponent caller;

	public SHERPA() {
		initComponent();
	}

	private void initComponent() {
		setLayout(new BorderLayout());
		initEditorPane();
		initToolBar();
		add(toolBar, BorderLayout.NORTH);
		add(new JScrollPane(editorPanePane), BorderLayout.CENTER);
	}

	private void initEditorPane() {
		editorPanePane = new JEditorPane();
		editorPanePane.setContentType("text/html");

		HTMLEditorKit kit = new HTMLEditorKit();

		// Récupération de la police globale App.fontGet()
		Font fontApp = App.fontGet();
		if (fontApp != null) {
			String rule = String.format("body { font-family: %s; font-size: %dpt; }",
					fontApp.getFamily(),
					fontApp.getSize());
			kit.getStyleSheet().addRule(rule);
		}

		editorPanePane.setEditorKit(kit);
		BindingsKey.installKeyBindings(editorPanePane);
	}

	private void initToolBar() {
		toolBar = new JToolBar();
		toolBar.setFloatable(false);

		initStyleActions();
		toolBar.addSeparator();
		initBlocActions();
		toolBar.addSeparator();
		initAlignmentActions();
	}

	/**
	 * initialize style actions
	 */
	private void initStyleActions() {
		// Select font button
		IconButton btnFont = new IconButton("sheFont", ICONS.K.SHE_FONT,
				"she.font", e -> {
					FontChooserDlg dlg = new FontChooserDlg(SwingUtilities.getWindowAncestor(this),
							htmlFontGet());
					dlg.setVisible(true);
					if (!dlg.isCanceled()) {
						Font selectedFont = dlg.getSelectedFont();
						setHtmlFont(dlg.getSelectedFont());
					}
				});
		toolBar.add(btnFont);
		// Text Color Button
		IconButton btnColorFg = new IconButton("sheColorFg", ICONS.K.SHE_COLOR,
				"she.color.text", e -> {
					JButton src = (JButton) e.getSource();
					JPopupMenu popup = createColorMenu(true);
					popup.show(src, 0, src.getHeight());
				});
		toolBar.add(btnColorFg);
		// Characters Style (short way buttons)
		Action actionGras = new StyledEditorKit.BoldAction();
		toolBar.add(createIconButton("sheBold", ICONS.K.SHE_TX_BOLD,
				"she.bold", actionGras));
		Action actionItalique = new StyledEditorKit.ItalicAction();
		toolBar.add(createIconButton("sheItalic", ICONS.K.SHE_TX_ITALIC,
				"she.italic", actionItalique));
		Action actionSouligne = new StyledEditorKit.UnderlineAction();
		toolBar.add(createIconButton("sheUnderline", ICONS.K.SHE_TX_UNDERLINE,
				"she.underline", actionSouligne));
		Action actionStrike = new HTMLEditorKit.InsertHTMLTextAction("", "<s></s>",
				HTML.Tag.BODY, HTML.getTag("s"));
		toolBar.add(createIconButton("sheStrike", ICONS.K.SHE_TX_STRIKE,
				"she.strike", actionStrike));
		Action actionSubscript = new HTMLEditorKit.InsertHTMLTextAction("", "<sub></sub>",
				HTML.Tag.BODY, HTML.getTag("sub"));
		toolBar.add(createIconButton("sheSubscript", ICONS.K.SHE_TX_SUBSCRIPT,
				"she.subscript", actionSubscript));
		Action actionSuperscript = new HTMLEditorKit.InsertHTMLTextAction("",
				"<sup></sup>", HTML.Tag.BODY, HTML.getTag("sup"));
		toolBar.add(createIconButton("sheSuperscript", ICONS.K.SHE_TX_SUPERSCRIPT,
				"she.superscript", actionSuperscript));
	}

	/**
	 * Récupère la police (nom et taille) à la position du curseur ou de la sélection.
	 *
	 * @return Font représentant la sélection/curseur courant
	 */
	public Font htmlFontGet() {
		int start = editorPanePane.getSelectionStart();
		StyledDocument doc = (StyledDocument) editorPanePane.getDocument();

		// 1. Récupération des attributs de la sélection ou du curseur
		javax.swing.text.AttributeSet attr;
		if (editorPanePane.getSelectionStart() != editorPanePane.getSelectionEnd()) {
			attr = doc.getCharacterElement(start).getAttributes();
		} else {
			attr = ((HTMLEditorKit) editorPanePane.getEditorKit()).getInputAttributes();
		}

		// 2. Extraire la famille et la taille si présentes dans les attributs
		String fontFamily = StyleConstants.getFontFamily(attr);
		int fontSize = StyleConstants.getFontSize(attr);

		// 3. Fallback sur la police globale si non défini dans l'attribut
		if (fontFamily == null || "SansSerif".equalsIgnoreCase(fontFamily)) {
			Font appFont = App.fontGet();
			if (appFont != null) {
				fontFamily = appFont.getFamily();
				if (fontSize == 12) { // 12 est la valeur par défaut de StyleConstants si absent
					fontSize = appFont.getSize();
				}
			} else {
				fontFamily = editorPanePane.getFont().getFamily();
			}
		}

		return new Font(fontFamily, Font.PLAIN, fontSize);
	}

	/**
	 * alignment actions
	 */
	private void initAlignmentActions() {
		Action actionGauche = new StyledEditorKit.AlignmentAction("",
				StyleConstants.ALIGN_LEFT);
		toolBar.add(createIconButton("sheAlignLeft", ICONS.K.SHE_AL_LEFT,
				"she.align.left", actionGauche));

		Action actionCentre = new StyledEditorKit.AlignmentAction("",
				StyleConstants.ALIGN_CENTER);
		toolBar.add(createIconButton("sheAlignCenter", ICONS.K.SHE_AL_CENTER,
				"she.align.center", actionCentre));

		Action actionDroite = new StyledEditorKit.AlignmentAction("",
				StyleConstants.ALIGN_RIGHT);
		toolBar.add(createIconButton("sheAlignRight", ICONS.K.SHE_AL_RIGHT,
				"she.align.right", actionDroite));
	}

	/**
	 * bloc actions
	 */
	private void initBlocActions() {
		BlockFormatItem[] items = new BlockFormatItem[]{
			new BlockFormatItem(I18N.getMsg("she.block.paragraph"),
			HTML.Tag.P, "<p></p>"),
			new BlockFormatItem(I18N.getMsg("she.para.h1"),
			HTML.Tag.H1, "<h1></h1>"),
			new BlockFormatItem(I18N.getMsg("she.para.h2"),
			HTML.Tag.H2, "<h2></h2>"),
			new BlockFormatItem(I18N.getMsg("she.para.h3"),
			HTML.Tag.H3, "<h3></h3>"),
			new BlockFormatItem(I18N.getMsg("she.para.h4"),
			HTML.Tag.H4, "<h4></h4>"),
			new BlockFormatItem(I18N.getMsg("she.para.h5"),
			HTML.Tag.H5, "<h5></h5>"),
			new BlockFormatItem(I18N.getMsg("she.para.h6"),
			HTML.Tag.H6, "<h6></h6>"),
			new BlockFormatItem(I18N.getMsg("she.block.pre"),
			HTML.Tag.PRE, "<pre></pre>"),
			new BlockFormatItem(I18N.getMsg("she.block.quote"),
			HTML.Tag.BLOCKQUOTE, "<blockquote></blockquote>"),
			new BlockFormatItem(I18N.getMsg("she.block.address"),
			HTML.Tag.ADDRESS, "<address></address>")
		};

		JComboBox<BlockFormatItem> cbBloc = new JComboBox<>(items);
		cbBloc.setMaximumSize(new Dimension(160, 26));
		cbBloc.setToolTipText(I18N.getMsg("she.block.format"));

		cbBloc.addActionListener(e -> {
			BlockFormatItem selected = (BlockFormatItem) cbBloc.getSelectedItem();
			if (selected != null) {
				Action insertAction = new HTMLEditorKit.InsertHTMLTextAction(
						"",
						selected.htmlSnippetGet(),
						HTML.Tag.BODY,
						selected.tagGet()
				);
				insertAction.actionPerformed(
						new ActionEvent(editorPanePane, e.getID(),
								e.getActionCommand()));
			}
		});

		toolBar.add(cbBloc);
		toolBar.addSeparator();
		// 3. Background Color Buttons
		IconButton btnColorBg = new IconButton("sheColorBg", ICONS.K.SHE_HIGHLIGHTER,
				"she.color.bg", e -> {
					JButton src = (JButton) e.getSource();
					JPopupMenu popup = createColorMenu(false);
					popup.show(src, 0, src.getHeight());
				});
		toolBar.add(btnColorBg);
		toolBar.addSeparator();

		Action actionPuce = new HTMLEditorKit.InsertHTMLTextAction("",
				"<ul><li> </li></ul>",
				HTML.Tag.BODY, HTML.Tag.UL);
		toolBar.add(createIconButton("sheListUnordered",
				ICONS.K.SHE_LIST_UNORDERED, "she.list.unordered", actionPuce));

		Action actionOrdered = new HTMLEditorKit.InsertHTMLTextAction("",
				"<ol><li> </li></ol>", HTML.Tag.BODY, HTML.Tag.OL);
		toolBar.add(createIconButton("sheListOrdered",
				ICONS.K.SHE_LIST_ORDERED, "she.list.ordered", actionOrdered));
	}

	/**
	 * PopupMenu representing a grid of 4x4 colored butons.
	 */
	private JPopupMenu createColorMenu(boolean isForeground) {
		JPopupMenu popup = new JPopupMenu();
		JPanel panel = new JPanel(new GridLayout(4, 4, 2, 2));

		for (ColorUtil.ColorItem item : ColorUtil.palette16Get()) {
			JButton colorBtn = new JButton();
			colorBtn.setPreferredSize(new Dimension(20, 20));
			colorBtn.setBackground(item.colorGet());
			colorBtn.setToolTipText(item.toString());
			colorBtn.setFocusable(false);

			colorBtn.addActionListener(e -> {
				if (isForeground) {
					Action colorAction = new StyledEditorKit.ForegroundAction("",
							item.colorGet());
					colorAction.actionPerformed(new ActionEvent(editorPanePane,
							ActionEvent.ACTION_PERFORMED, item.hexGet()));
				} else {
					int start = editorPanePane.getSelectionStart();
					int end = editorPanePane.getSelectionEnd();
					String selectedText = editorPanePane.getSelectedText();
					if (selectedText == null || selectedText.isEmpty()) {
						selectedText = "&nbsp;";
					} else {
						selectedText = selectedText.replace("&", "&amp;")
								.replace("<", "&lt;")
								.replace(">", "&gt;");
					}

					String htmlSpan = "<span style=\"background-color: "
							+ item.hexGet() + ";\">" + selectedText + "</span>";

					Action bgAction = new HTMLEditorKit.InsertHTMLTextAction(
							"",
							htmlSpan,
							HTML.Tag.P,
							HTML.Tag.SPAN
					);

					if (end > start) {
						editorPanePane.replaceSelection("");
					}

					bgAction.actionPerformed(new ActionEvent(editorPanePane,
							ActionEvent.ACTION_PERFORMED, item.hexGet()));
				}
				popup.setVisible(false);
			});
			panel.add(colorBtn);
		}

		popup.add(panel);
		return popup;
	}

	/**
	 * create an icon button
	 *
	 * @param name
	 * @param iconKey
	 * @param tooltipKey
	 * @param action
	 * @return
	 */
	private IconButton createIconButton(String name, ICONS.K iconKey,
			String tooltipKey, Action action) {
		action.putValue(Action.NAME, "");
		return new IconButton(name, iconKey, tooltipKey, action);
	}

	/**
	 * get HTML body content
	 *
	 * @return
	 */
	public String htmlContentGet() {
		String fullHtml = editorPanePane.getText();
		if (fullHtml == null) {
			return "";
		}

		int bodyStart = fullHtml.indexOf("<body>");
		int bodyEnd = fullHtml.indexOf("</body>");

		if (bodyStart != -1 && bodyEnd != -1) {
			return fullHtml.substring(bodyStart + 6, bodyEnd).trim();
		}

		return fullHtml.trim();
	}

	/**
	 * set HTML content
	 *
	 * @param html
	 */
	public void htmlContentSet(String html) {
		if (html == null || html.trim().isEmpty()) {
			editorPanePane.setText("<html><body><p></p></body></html>");
			return;
		}

		String cleanedHtml = html.trim();

		if (!cleanedHtml.toLowerCase().startsWith("<html>")) {
			if (!cleanedHtml.contains("<") && !cleanedHtml.contains(">")) {
				cleanedHtml = cleanedHtml.replace("&", "&amp;")
						.replace("<", "&lt;")
						.replace(">", "&gt;");
			}
			cleanedHtml = "<html><body>" + cleanedHtml + "</body></html>";
		}

		editorPanePane.setText(cleanedHtml);
		editorPanePane.setCaretPosition(0);
	}

	/**
	 * get the editor pane
	 *
	 * @return
	 */
	public JEditorPane editorPaneGet() {
		return editorPanePane;
	}

	/**
	 * hide tool bar
	 */
	public void toolbarHide() {
		if (toolBar.isVisible()) {
			toolBar.setVisible(false);
			revalidate();
			repaint();
		}
	}

	/**
	 * show tool bar
	 */
	public void toolbarShow() {
		if (!toolBar.isVisible()) {
			toolBar.setVisible(true);
			revalidate();
			repaint();
		}
	}

	private void setHtmlFont(Font font) {
		editorPanePane.requestFocusInWindow();

		SimpleAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setFontFamily(attr, font.getFamily());
		StyleConstants.setFontSize(attr, font.getSize());

		int start = editorPanePane.getSelectionStart();
		int end = editorPanePane.getSelectionEnd();
		int length = end - start;

		StyledDocument doc = (StyledDocument) editorPanePane.getDocument();
		if (length > 0) {
			doc.setCharacterAttributes(start, length, attr, false);
		} else {
			((HTMLEditorKit) editorPanePane
					.getEditorKit()).getInputAttributes().addAttributes(attr);
		}

	}

	/**
	 * utility Class for cloros.
	 */
	public static class ColorUtil {

		public static class ColorItem {

			private final String name;
			private final Color color;
			private final String hex;

			public ColorItem(String nameKey, Color color, String hex) {
				this.name = I18N.getMsg(nameKey);
				this.color = color;
				this.hex = hex;
			}

			public Color colorGet() {
				return color;
			}

			public String hexGet() {
				return hex;
			}

			@Override
			public String toString() {
				return name;
			}
		}

		public static ColorItem[] palette16Get() {
			return new ColorItem[]{
				new ColorItem("she.color.black", Color.BLACK, "#000000"),
				new ColorItem("she.color.white", Color.WHITE, "#FFFFFF"),
				new ColorItem("she.color.darkgray", Color.DARK_GRAY, "#A9A9A9"),
				new ColorItem("she.color.gray", Color.GRAY, "#808080"),
				new ColorItem("she.color.lightgray", Color.LIGHT_GRAY, "#D3D3D3"),
				new ColorItem("she.color.red", Color.RED, "#FF0000"),
				new ColorItem("she.color.green", Color.GREEN, "#008000"),
				new ColorItem("she.color.blue", Color.BLUE, "#0000FF"),
				new ColorItem("she.color.yellow", Color.YELLOW, "#FFFF00"),
				new ColorItem("she.color.magenta", Color.MAGENTA, "#FF00FF"),
				new ColorItem("she.color.cyan", Color.CYAN, "#00FFFF"),
				new ColorItem("she.color.orange", Color.ORANGE, "#FFA500"),
				new ColorItem("she.color.pink", Color.PINK, "#FFC0CB"),
				new ColorItem("she.color.brown", new Color(139, 69, 19), "#8B4513"),
				new ColorItem("she.color.purple", new Color(128, 0, 128), "#800080"),
				new ColorItem("she.color.navy", new Color(0, 0, 128), "#000080")
			};
		}
	}

	/**
	 * utility class to format a bloc
	 */
	private static class BlockFormatItem {

		private final String label;
		private final HTML.Tag tag;
		private final String htmlSnippet;

		public BlockFormatItem(String label, HTML.Tag tag, String htmlSnippet) {
			this.label = label;
			this.tag = tag;
			this.htmlSnippet = htmlSnippet;
		}

		public HTML.Tag tagGet() {
			return tag;
		}

		public String htmlSnippetGet() {
			return htmlSnippet;
		}

		@Override
		public String toString() {
			return label;
		}
	}

}
