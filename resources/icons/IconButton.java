/*
Storybook: Scene-based software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package resources.icons;

import i18n.I18N;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

@SuppressWarnings("serial")
public class IconButton extends JButton {

	public IconButton() {
		super();
	}

	public IconButton(String name, ICONS.K icon, Action action) {
		super(action);
		this.setName(name);
		Icon ic = IconUtil.getIconSmall(icon);
		if (icon != ICONS.K.NONE) {
			setIcon(ic);
		}
	}

	public IconButton(String name, ICONS.K icon, String tips, Action action) {
		this(name, icon, action);
		if (tips != null && !tips.isEmpty()) {
			this.setToolTipText(I18N.getMsg(tips));
		}
	}

	public IconButton(String name, ICONS.K icon, ActionListener action) {
		super();
		this.setName(name);
		if (icon != ICONS.K.NONE) {
			setIcon(IconUtil.getIconSmall(icon));
		}
		this.addActionListener(action);
		this.setMargin(new Insets(0, 0, 0, 0));
	}

	public IconButton(String name, ICONS.K icon, String tips, ActionListener action) {
		this(name, icon, action);
		if (tips != null && !tips.isEmpty()) {
			this.setToolTipText(I18N.getMsg(tips));
		}
	}

	public void setFlat() {
		setBorderPainted(false);
		setOpaque(false);
		setContentAreaFilled(false);
	}

}
