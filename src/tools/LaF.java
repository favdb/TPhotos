/*
 * Copyright (C) 2024 favdb
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
package tools;

import app.App;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.Enumeration;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 * class to manage the LaF
 *
 * @author favdb
 */
public class LaF {

	private static final String TT = "Laf.";

	public static int getScreenWidth() {
		return Toolkit.getDefaultToolkit().getScreenSize().width;
	}

	public static int getScreenHeight() {
		return Toolkit.getDefaultToolkit().getScreenSize().height;
	}

	public enum THEME {
		activeCaptionBorder(new Color(163, 184, 204)),
		activeCaption(new Color(184, 207, 229)),
		activeCaptionText(new Color(51, 51, 51)),
		Button_background(new Color(238, 238, 238)),
		Button_darkShadow(new Color(122, 138, 153)),
		Button_disabledText(new Color(153, 153, 153)),
		Button_disabledToolBarBorderBackground(new Color(204, 204, 204)),
		Button_focus(new Color(163, 184, 204)),
		Button_foreground(new Color(51, 51, 51)),
		Button_highlight(new Color(255, 255, 255)),
		Button_light(new Color(255, 255, 255)),
		Button_select(new Color(184, 207, 229)),
		Button_shadow(new Color(184, 207, 229)),
		Button_toolBarBorderBackground(new Color(153, 153, 153)),
		CheckBox_background(new Color(238, 238, 238)),
		CheckBox_disabledText(new Color(153, 153, 153)),
		CheckBox_focus(new Color(163, 184, 204)),
		CheckBox_foreground(new Color(51, 51, 51)),
		CheckBoxMenuItem_acceleratorForeground(new Color(99, 130, 191)),
		CheckBoxMenuItem_acceleratorSelectionForeground(new Color(51, 51, 51)),
		CheckBoxMenuItem_background(new Color(238, 238, 238)),
		CheckBoxMenuItem_disabledForeground(new Color(153, 153, 153)),
		CheckBoxMenuItem_foreground(new Color(51, 51, 51)),
		CheckBoxMenuItem_selectionBackground(new Color(163, 184, 204)),
		CheckBoxMenuItem_selectionForeground(new Color(51, 51, 51)),
		Checkbox_select(new Color(184, 207, 229)),
		ColorChooser_background(new Color(238, 238, 238)),
		ColorChooser_foreground(new Color(51, 51, 51)),
		ColorChooser_swatchesDefaultRecentColor(new Color(238, 238, 238)),
		ComboBox_background(new Color(238, 238, 238)),
		ComboBox_buttonBackground(new Color(238, 238, 238)),
		ComboBox_buttonDarkShadow(new Color(122, 138, 153)),
		ComboBox_buttonHighlight(new Color(255, 255, 255)),
		ComboBox_buttonShadow(new Color(184, 207, 229)),
		ComboBox_disabledBackground(new Color(238, 238, 238)),
		ComboBox_disabledForeground(new Color(184, 207, 229)),
		ComboBox_foreground(new Color(51, 51, 51)),
		ComboBox_selectionBackground(new Color(163, 184, 204)),
		ComboBox_selectionForeground(new Color(51, 51, 51)),
		controlDkShadow(new Color(122, 138, 153)),
		controlHighlight(new Color(255, 255, 255)),
		controlLtHighlight(new Color(255, 255, 255)),
		control(new Color(238, 238, 238)),
		controlShadow(new Color(184, 207, 229)),
		controlText(new Color(51, 51, 51)),
		Desktop_background(new Color(255, 255, 255)),
		DesktopIcon_background(new Color(238, 238, 238)),
		DesktopIcon_foreground(new Color(51, 51, 51)),
		desktop(new Color(255, 255, 255)),
		EditorPane_background(new Color(255, 255, 255)),
		EditorPane_caretForeground(new Color(51, 51, 51)),
		EditorPane_foreground(new Color(51, 51, 51)),
		EditorPane_inactiveForeground(new Color(184, 207, 229)),
		EditorPane_selectionBackground(new Color(184, 207, 229)),
		EditorPane_selectionForeground(new Color(51, 51, 51)),
		FormattedTextField_background(new Color(255, 255, 255)),
		FormattedTextField_caretForeground(new Color(51, 51, 51)),
		FormattedTextField_foreground(new Color(51, 51, 51)),
		FormattedTextField_inactiveBackground(new Color(238, 238, 238)),
		FormattedTextField_inactiveForeground(new Color(184, 207, 229)),
		FormattedTextField_selectionBackground(new Color(184, 207, 229)),
		FormattedTextField_selectionForeground(new Color(51, 51, 51)),
		inactiveCaptionBorder(new Color(184, 207, 229)),
		inactiveCaption(new Color(238, 238, 238)),
		inactiveCaptionText(new Color(51, 51, 51)),
		info(new Color(184, 207, 229)),
		infoText(new Color(51, 51, 51)),
		InternalFrame_activeTitleBackground(new Color(184, 207, 229)),
		InternalFrame_activeTitleForeground(new Color(51, 51, 51)),
		InternalFrame_borderColor(new Color(238, 238, 238)),
		InternalFrame_borderDarkShadow(new Color(122, 138, 153)),
		InternalFrame_borderHighlight(new Color(255, 255, 255)),
		InternalFrame_borderLight(new Color(255, 255, 255)),
		InternalFrame_borderShadow(new Color(184, 207, 229)),
		InternalFrame_inactiveTitleBackground(new Color(238, 238, 238)),
		InternalFrame_inactiveTitleForeground(new Color(51, 51, 51)),
		Label_background(new Color(238, 238, 238)),
		Label_disabledForeground(new Color(153, 153, 153)),
		Label_disabledShadow(new Color(184, 207, 229)),
		Label_foreground(new Color(51, 51, 51)),
		List_background(new Color(255, 255, 255)),
		List_dropCellBackground(new Color(210, 233, 255)),
		List_dropLineColor(new Color(99, 130, 191)),
		List_foreground(new Color(51, 51, 51)),
		List_selectionBackground(new Color(184, 207, 229)),
		List_selectionForeground(new Color(51, 51, 51)),
		Menu_acceleratorForeground(new Color(99, 130, 191)),
		Menu_acceleratorSelectionForeground(new Color(51, 51, 51)),
		Menu_background(new Color(238, 238, 238)),
		MenuBar_background(new Color(238, 238, 238)),
		MenuBar_borderColor(new Color(204, 204, 204)),
		MenuBar_foreground(new Color(51, 51, 51)),
		MenuBar_highlight(new Color(255, 255, 255)),
		MenuBar_shadow(new Color(184, 207, 229)),
		Menu_disabledForeground(new Color(153, 153, 153)),
		Menu_foreground(new Color(51, 51, 51)),
		MenuItem_acceleratorForeground(new Color(99, 130, 191)),
		MenuItem_acceleratorSelectionForeground(new Color(51, 51, 51)),
		MenuItem_background(new Color(238, 238, 238)),
		MenuItem_disabledForeground(new Color(153, 153, 153)),
		MenuItem_foreground(new Color(51, 51, 51)),
		MenuItem_selectionBackground(new Color(163, 184, 204)),
		MenuItem_selectionForeground(new Color(51, 51, 51)),
		menu(new Color(238, 238, 238)),
		Menu_selectionBackground(new Color(163, 184, 204)),
		Menu_selectionForeground(new Color(51, 51, 51)),
		menuText(new Color(51, 51, 51)),
		OptionPane_background(new Color(238, 238, 238)),
		OptionPane_errorDialog_border_background(new Color(153, 51, 51)),
		OptionPane_errorDialog_titlePane_background(new Color(255, 153, 153)),
		OptionPane_errorDialog_titlePane_foreground(new Color(51, 0, 0)),
		OptionPane_errorDialog_titlePane_shadow(new Color(204, 102, 102)),
		OptionPane_foreground(new Color(51, 51, 51)),
		OptionPane_messageForeground(new Color(51, 51, 51)),
		OptionPane_questionDialog_border_background(new Color(51, 102, 51)),
		OptionPane_questionDialog_titlePane_background(new Color(153, 204, 153)),
		OptionPane_questionDialog_titlePane_foreground(new Color(0, 51, 0)),
		OptionPane_questionDialog_titlePane_shadow(new Color(102, 153, 102)),
		OptionPane_warningDialog_border_background(new Color(153, 102, 51)),
		OptionPane_warningDialog_titlePane_background(new Color(255, 204, 153)),
		OptionPane_warningDialog_titlePane_foreground(new Color(102, 51, 0)),
		OptionPane_warningDialog_titlePane_shadow(new Color(204, 153, 102)),
		Panel_background(new Color(238, 238, 238)),
		Panel_foreground(new Color(51, 51, 51)),
		PasswordField_background(new Color(255, 255, 255)),
		PasswordField_caretForeground(new Color(51, 51, 51)),
		PasswordField_foreground(new Color(51, 51, 51)),
		PasswordField_inactiveBackground(new Color(238, 238, 238)),
		PasswordField_inactiveForeground(new Color(184, 207, 229)),
		PasswordField_selectionBackground(new Color(184, 207, 229)),
		PasswordField_selectionForeground(new Color(51, 51, 51)),
		PopupMenu_background(new Color(238, 238, 238)),
		PopupMenu_foreground(new Color(51, 51, 51)),
		ProgressBar_background(new Color(238, 238, 238)),
		ProgressBar_foreground(new Color(163, 184, 204)),
		ProgressBar_selectionBackground(new Color(99, 130, 191)),
		ProgressBar_selectionForeground(new Color(238, 238, 238)),
		RadioButton_background(new Color(238, 238, 238)),
		RadioButton_darkShadow(new Color(122, 138, 153)),
		RadioButton_disabledText(new Color(153, 153, 153)),
		RadioButton_focus(new Color(163, 184, 204)),
		RadioButton_foreground(new Color(51, 51, 51)),
		RadioButton_highlight(new Color(255, 255, 255)),
		RadioButton_light(new Color(255, 255, 255)),
		RadioButtonMenuItem_acceleratorForeground(new Color(99, 130, 191)),
		RadioButtonMenuItem_acceleratorSelectionForeground(new Color(51, 51, 51)),
		RadioButtonMenuItem_background(new Color(238, 238, 238)),
		RadioButtonMenuItem_disabledForeground(new Color(153, 153, 153)),
		RadioButtonMenuItem_foreground(new Color(51, 51, 51)),
		RadioButtonMenuItem_selectionBackground(new Color(163, 184, 204)),
		RadioButtonMenuItem_selectionForeground(new Color(51, 51, 51)),
		RadioButton_select(new Color(184, 207, 229)),
		RadioButton_shadow(new Color(184, 207, 229)),
		ScrollBar_background(new Color(238, 238, 238)),
		ScrollBar_darkShadow(new Color(122, 138, 153)),
		ScrollBar_foreground(new Color(238, 238, 238)),
		ScrollBar_highlight(new Color(255, 255, 255)),
		scrollbar(new Color(238, 238, 238)),
		ScrollBar_shadow(new Color(184, 207, 229)),
		ScrollBar_thumbDarkShadow(new Color(122, 138, 153)),
		ScrollBar_thumbHighlight(new Color(184, 207, 229)),
		ScrollBar_thumb(new Color(163, 184, 204)),
		ScrollBar_thumbShadow(new Color(99, 130, 191)),
		ScrollBar_trackHighlight(new Color(122, 138, 153)),
		ScrollBar_track(new Color(238, 238, 238)),
		ScrollPane_background(new Color(238, 238, 238)),
		ScrollPane_foreground(new Color(51, 51, 51)),
		Separator_background(new Color(255, 255, 255)),
		Separator_foreground(new Color(99, 130, 191)),
		Separator_highlight(new Color(255, 255, 255)),
		Separator_shadow(new Color(184, 207, 229)),
		Slider_altTrackColor(new Color(210, 226, 239)),
		Slider_background(new Color(238, 238, 238)),
		Slider_focus(new Color(163, 184, 204)),
		Slider_foreground(new Color(163, 184, 204)),
		Slider_highlight(new Color(255, 255, 255)),
		Slider_shadow(new Color(184, 207, 229)),
		Slider_tickColor(new Color(0, 0, 0)),
		Spinner_background(new Color(238, 238, 238)),
		Spinner_foreground(new Color(238, 238, 238)),
		SplitPane_background(new Color(238, 238, 238)),
		SplitPane_darkShadow(new Color(122, 138, 153)),
		SplitPaneDivider_draggingColor(new Color(64, 64, 64)),
		SplitPane_dividerFocusColor(new Color(200, 221, 242)),
		SplitPane_highlight(new Color(255, 255, 255)),
		SplitPane_shadow(new Color(184, 207, 229)),
		TabbedPane_background(new Color(184, 207, 229)),
		TabbedPane_borderHightlightColor(new Color(99, 130, 191)),
		TabbedPane_contentAreaColor(new Color(200, 221, 242)),
		TabbedPane_darkShadow(new Color(122, 138, 153)),
		TabbedPane_focus(new Color(99, 130, 191)),
		TabbedPane_foreground(new Color(51, 51, 51)),
		TabbedPane_highlight(new Color(255, 255, 255)),
		TabbedPane_light(new Color(238, 238, 238)),
		TabbedPane_selected(new Color(200, 221, 242)),
		TabbedPane_selectHighlight(new Color(255, 255, 255)),
		TabbedPane_shadow(new Color(184, 207, 229)),
		TabbedPane_tabAreaBackground(new Color(218, 218, 218)),
		TabbedPane_unselectedBackground(new Color(238, 238, 238)),
		Table_background(new Color(255, 255, 255)),
		Table_dropCellBackground(new Color(210, 233, 255)),
		Table_dropLineColor(new Color(99, 130, 191)),
		Table_dropLineShortColor(new Color(51, 51, 51)),
		Table_focusCellBackground(new Color(255, 255, 255)),
		Table_focusCellForeground(new Color(51, 51, 51)),
		Table_foreground(new Color(51, 51, 51)),
		Table_gridColor(new Color(122, 138, 153)),
		TableHeader_background(new Color(238, 238, 238)),
		TableHeader_focusCellBackground(new Color(200, 221, 242)),
		TableHeader_foreground(new Color(51, 51, 51)),
		Table_selectionBackground(new Color(184, 207, 229)),
		Table_selectionForeground(new Color(51, 51, 51)),
		Table_sortIconColor(new Color(184, 207, 229)),
		TextArea_background(new Color(255, 255, 255)),
		TextArea_caretForeground(new Color(51, 51, 51)),
		TextArea_foreground(new Color(51, 51, 51)),
		TextArea_inactiveForeground(new Color(184, 207, 229)),
		TextArea_selectionBackground(new Color(184, 207, 229)),
		TextArea_selectionForeground(new Color(51, 51, 51)),
		TextField_background(new Color(255, 255, 255)),
		TextField_caretForeground(new Color(51, 51, 51)),
		TextField_darkShadow(new Color(122, 138, 153)),
		TextField_foreground(new Color(51, 51, 51)),
		TextField_highlight(new Color(255, 255, 255)),
		TextField_inactiveBackground(new Color(238, 238, 238)),
		TextField_inactiveForeground(new Color(184, 207, 229)),
		TextField_light(new Color(255, 255, 255)),
		TextField_selectionBackground(new Color(184, 207, 229)),
		TextField_selectionForeground(new Color(51, 51, 51)),
		TextField_shadow(new Color(184, 207, 229)),
		textHighlight(new Color(184, 207, 229)),
		textHighlightText(new Color(51, 51, 51)),
		textInactiveText(new Color(184, 207, 229)),
		text(new Color(255, 255, 255)),
		TextPane_background(new Color(255, 255, 255)),
		TextPane_caretForeground(new Color(51, 51, 51)),
		TextPane_foreground(new Color(51, 51, 51)),
		TextPane_inactiveForeground(new Color(184, 207, 229)),
		TextPane_selectionBackground(new Color(184, 207, 229)),
		TextPane_selectionForeground(new Color(51, 51, 51)),
		textText(new Color(51, 51, 51)),
		TitledBorder_titleColor(new Color(51, 51, 51)),
		ToggleButton_background(new Color(238, 238, 238)),
		ToggleButton_darkShadow(new Color(122, 138, 153)),
		ToggleButton_disabledText(new Color(153, 153, 153)),
		ToggleButton_focus(new Color(163, 184, 204)),
		ToggleButton_foreground(new Color(51, 51, 51)),
		ToggleButton_highlight(new Color(255, 255, 255)),
		ToggleButton_light(new Color(255, 255, 255)),
		ToggleButton_select(new Color(184, 207, 229)),
		ToggleButton_shadow(new Color(184, 207, 229)),
		ToolBar_background(new Color(238, 238, 238)),
		ToolBar_borderColor(new Color(204, 204, 204)),
		ToolBar_darkShadow(new Color(122, 138, 153)),
		ToolBar_dockingBackground(new Color(238, 238, 238)),
		ToolBar_dockingForeground(new Color(99, 130, 191)),
		ToolBar_floatingBackground(new Color(238, 238, 238)),
		ToolBar_floatingForeground(new Color(184, 207, 229)),
		ToolBar_foreground(new Color(51, 51, 51)),
		ToolBar_highlight(new Color(255, 255, 255)),
		ToolBar_light(new Color(255, 255, 255)),
		ToolBar_shadow(new Color(184, 207, 229)),
		ToolTip_backgroundInactive(new Color(238, 238, 238)),
		ToolTip_background(new Color(184, 207, 229)),
		ToolTip_foregroundInactive(new Color(122, 138, 153)),
		ToolTip_foreground(new Color(51, 51, 51)),
		Tree_background(new Color(255, 255, 255)),
		Tree_dropCellBackground(new Color(210, 233, 255)),
		Tree_dropLineColor(new Color(99, 130, 191)),
		Tree_foreground(new Color(51, 51, 51)),
		Tree_hash(new Color(184, 207, 229)),
		Tree_line(new Color(184, 207, 229)),
		Tree_selectionBackground(new Color(184, 207, 229)),
		Tree_selectionBorderColor(new Color(99, 130, 191)),
		Tree_selectionForeground(new Color(51, 51, 51)),
		Tree_textBackground(new Color(255, 255, 255)),
		Tree_textForeground(new Color(51, 51, 51)),
		Viewport_background(new Color(238, 238, 238)),
		Viewport_foreground(new Color(51, 51, 51)),
		windowBorder(new Color(238, 238, 238)),
		window(new Color(255, 255, 255)),
		windowText(new Color(51, 51, 51));
		Color normal;

		THEME(Color normal) {
			this.normal = normal;
		}

		public Color getNormal() {
			return normal;
		}

		public Color get(boolean b) {
			return (b ? invertColor(normal) : getNormal());
		}

		public Color getDark() {
			return invertColor(normal);
		}
	}

	/**
	 * put the given THEME
	 *
	 * @param tk
	 */
	private static void put(THEME tk) {
		UIManager.put(tk.name().replace("_", "."), tk.get(App.preferences.darkGet()));
	}

	/**
	 * load the Nimbus LaF and initialize
	 */
	public static void init() {
		setFont();
		if (App.preferences.darkGet()) {
			setColors();
		}
	}

	/**
	 * init colors for the LaF
	 */
	public static void setColors() {
		// empty
	}

	private static Color invertColor(Color c) {
		return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue());
	}

	/**
	 * init default Font
	 */
	public static void setFont() {
		UIDefaults defaults = UIManager.getDefaults();
		Enumeration newKeys = defaults.keys();
		while (newKeys.hasMoreElements()) {
			Object obj = newKeys.nextElement();
			Object current = UIManager.get(obj);
			if (current instanceof FontUIResource) {
				defaults.put(obj, new FontUIResource(App.fontGet()));
			} else if (current instanceof Font) {
				defaults.put(obj, App.fontGet());
			}
		}
	}

	/**
	 * update the LaF
	 */
	public static void update() {
		for (Window w : Window.getWindows()) {
			SwingUtilities.updateComponentTreeUI(w);
		}
	}

	private static boolean DARK_THEME = false;

	public static boolean isDark() {
		return DARK_THEME;
	}

}
