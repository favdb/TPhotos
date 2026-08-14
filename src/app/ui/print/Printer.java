package app.ui.print;

import app.i18n.I18N;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.JOptionPane;
import app.tools.LOG;

/**
 * Controller for managing hardware printing via Java Print Service.
 *
 * @author favdb
 */
public class Printer {

	private static final String TT = "PrintEngine.";

	public static PrintService[] getAvailablePrinters() {
		return PrintServiceLookup.lookupPrintServices(null, null);
	}

	public static PrintService getDefaultPrinter() {
		return PrintServiceLookup.lookupDefaultPrintService();
	}

	public static void executePrint(final Print print) {
		executePrint(print, null);
	}

	public static void executePrint(final Print print, final PrintService targetPrinter) {
		if (print == null) {
			LOG.err(TT + "executePrint: Print model is null");
			return;
		}
		PrinterJob job = PrinterJob.getPrinterJob();
		GridPrint doc = new GridPrint(print);
		PageFormat pf = doc.getPageFormat(0);
		job.setPageable(doc);
		PrintRequestAttributeSet attribs = new HashPrintRequestAttributeSet();
		if ("A3".equalsIgnoreCase(print.paperFormatGet())) {
			attribs.add(MediaSizeName.ISO_A3);
		} else {
			attribs.add(MediaSizeName.ISO_A4);
		}
		if (Print.LANDSCAPE.equalsIgnoreCase(print.paperOrientationGet())) {
			attribs.add(OrientationRequested.LANDSCAPE);
		} else {
			attribs.add(OrientationRequested.PORTRAIT);
		}
		boolean proc = false;
		try {
			if (targetPrinter != null) {
				job.setPrintService(targetPrinter);
				proc = true;
			} else {
				proc = job.printDialog(attribs);
			}
		} catch (PrinterException e) {
			LOG.err(TT + "Error setting print service", e);
			JOptionPane.showMessageDialog(print,
					I18N.getMsg("print.error_msg", e.getMessage()),
					I18N.getMsg("print.error"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (proc) {
			new Thread(() -> {
				try {
					job.print(attribs);
				} catch (PrinterException e) {
					LOG.err(TT + "Error during printing", e);
					JOptionPane.showMessageDialog(print,
							I18N.getMsg("print.error_msg", e.getMessage()),
							I18N.getMsg("print.error"),
							JOptionPane.ERROR_MESSAGE);
				}
			}).start();
		}
	}

	public static PrintService findPrinterByName(String name) {
		if (name == null || name.trim().isEmpty()) {
			return null;
		}
		PrintService[] services = getAvailablePrinters();
		for (PrintService service : services) {
			if (service.getName().equalsIgnoreCase(name)
					|| service.getName().contains(name)) {
				return service;
			}
		}
		return null;
	}

}
