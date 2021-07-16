package mike.utilties;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Resource {

	private Path resource = null;
	private boolean external = false;
	
	public Resource(String filename) {
		if (filename != null && ! filename.trim().isEmpty() )
			this.resource(Paths.get(filename));
	}
	
	public Resource(Path file) {
		if ( file != null && ! file.toString().isEmpty() )
		this.resource(file);
	}
	
	public boolean exists() {
		return resource != null;
	}
	
	public boolean notExists() {
		return resource == null;
	}
	
	public boolean isExternal() {
		return external;
	}
	
	public Path getResource() {
		return this.resource;
	}
	
	public InputStream getInputStream() throws IOException {
		if ( external )
			return Files.newInputStream(resource);
		else
			return Resource.class.getClassLoader().getResourceAsStream(resource.toString());
	}
	
	public Properties getProperties() throws IOException {
		Properties properties = new Properties();
		
		if ( this.exists() ) {
			try ( InputStream is = this.getInputStream(); ) {
				properties.load(is);
			}
		}
		
		return properties;
	}
	
	private void resource(Path file) {
		
		if ( Files.isReadable(file) ) {
			this.resource = file;
			this.external = true;
		} else {
			URL url = Resource.class.getClassLoader().getResource(file.toString());
			if ( url != null )
				this.resource = file;
		}
	}
}
