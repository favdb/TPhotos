package app.organize;

import java.io.File;

public class OrganizerPath {

	private final File source;
	private final File destination;
	private final boolean isTriage;

	public OrganizerPath(File source, File destination, boolean isTriage) {
		this.source = source;
		this.destination = destination;
		this.isTriage = isTriage;
	}

	public File getSource() {
		return source;
	}

	public File getDestination() {
		return destination;
	}

	public boolean isTriage() {
		return isTriage;
	}
}
