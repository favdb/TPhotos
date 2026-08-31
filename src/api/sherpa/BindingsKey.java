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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

/**
 * shorcuts for SHE
 *
 * @author favdb
 */
public class BindingsKey {

	/**
	 * Configure InputMap and ActionMap of JEditorPane for SHE.
	 *
	 * @param editorPane
	 */
	public static void installKeyBindings(JEditorPane editorPane) {
		InputMap inputMap = editorPane.getInputMap(JComponent.WHEN_FOCUSED);
		// Style of text
		bind(editorPane, inputMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK),
				"she.bold",
				new StyledEditorKit.BoldAction());

		bind(editorPane, inputMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK),
				"she.italic",
				new StyledEditorKit.ItalicAction());

		bind(editorPane, inputMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK),
				"she.underline",
				new StyledEditorKit.UnderlineAction());

		// bloc and lists
		bind(editorPane, inputMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_DOWN_MASK),
				"she.p",
				new HTMLEditorKit.InsertHTMLTextAction("", "<p></p>",
						HTML.Tag.BODY, HTML.Tag.P));

		bind(editorPane, inputMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_DOWN_MASK),
				"she.ol",
				new HTMLEditorKit.InsertHTMLTextAction("", "<ol><li> </li></ol>",
						HTML.Tag.BODY, HTML.Tag.OL));

		bind(editorPane, inputMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_DOWN_MASK),
				"she.ul",
				new HTMLEditorKit.InsertHTMLTextAction("", "<ul><li> </li></ul>",
						HTML.Tag.BODY, HTML.Tag.UL));

		// Titles H1 to H6 (numeric pad)
		int ctrlAlt = InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK;

		bind(editorPane, inputMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, ctrlAlt),
				"she.h1",
				new HTMLEditorKit.InsertHTMLTextAction("", "<h1></h1>",
						HTML.Tag.BODY, HTML.Tag.H1));

		bind(editorPane, inputMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, ctrlAlt),
				"she.h2",
				new HTMLEditorKit.InsertHTMLTextAction("", "<h2></h2>",
						HTML.Tag.BODY, HTML.Tag.H2));

		bind(editorPane, inputMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, ctrlAlt),
				"she.h3",
				new HTMLEditorKit.InsertHTMLTextAction("", "<h3></h3>",
						HTML.Tag.BODY, HTML.Tag.H3));

		bind(editorPane, inputMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, ctrlAlt),
				"she.h4",
				new HTMLEditorKit.InsertHTMLTextAction("",
						"<h4></h4>", HTML.Tag.BODY, HTML.Tag.H4));

		bind(editorPane, inputMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5, ctrlAlt),
				"she.h5",
				new HTMLEditorKit.InsertHTMLTextAction("", "<h5></h5>",
						HTML.Tag.BODY, HTML.Tag.H5));

		bind(editorPane, inputMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, ctrlAlt),
				"she.h6",
				new HTMLEditorKit.InsertHTMLTextAction("", "<h6></h6>",
						HTML.Tag.BODY, HTML.Tag.H6));

		// break line(<br>) with Ctrl+Enter
		bind(editorPane, inputMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK),
				"she.br",
				new HTMLEditorKit.InsertHTMLTextAction("", "<br>",
						HTML.Tag.BODY, HTML.Tag.BR));
		// break space(&nbsp;) with Ctrl+Space
		bind(editorPane, inputMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK),
				"she.space",
				new javax.swing.AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				editorPane.replaceSelection("\u00A0");
			}
		});
	}

	private static void bind(JEditorPane editorPane,
			InputMap inputMap, KeyStroke keyStroke,
			String actionKey, Action action) {
		inputMap.put(keyStroke, actionKey);
		editorPane.getActionMap().put(actionKey, action);
	}

}
