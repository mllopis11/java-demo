package com.sg.rdj.mapper.commons.jdbc.oracle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SQLLoaderTemplate {

	private final Map<String, List<String>> templates = new ConcurrentHashMap<String, List<String>>();
	
	public  void load(String filename) throws IOException {
		String fName = filename != null ? filename.trim() : "";
		this.load(Paths.get(fName));
	}
	
	public void load(Path file) throws IOException {
		
		if ( file == null ) {
			throw new IOException("SQLLoaderTemplate::load: no such file provided"); 
		}

		if ( Files.exists(file) ) {
			try ( InputStream is = Files.newInputStream(file); ) {
				this.load(is);
			}
		} else {
			ClassLoader cl = SQLLoaderTemplate.class.getClassLoader();
			URL url = cl.getResource(file.toString());
			if ( url == null )
				throw new IOException("no such file in classpath: " + file);
			
			try ( InputStream is = cl.getResourceAsStream(file.toString())) {
				this.load(is);
			}
		}
	}
	
	public void load(InputStream is) {
		
		final String startPattern = "@ID=";
		
		try ( Scanner sc = new Scanner(is); ) {
			
			String keyword = "";
			List<String> template = null;
			
			while ( sc.hasNextLine() ) {
				String line = sc.nextLine().trim();
				
				if ( line.startsWith(startPattern) ) {
					if ( template != null ) 
						templates.put(keyword, template);
						
					keyword = line.replaceFirst(startPattern, "");
					if ( keyword.isEmpty() )
						keyword = "DEFAULT";
						
					template = new ArrayList<>();
					
				} else if ( ! keyword.isEmpty() && ! line.isEmpty() && ! line.startsWith("--") && ! line.startsWith("#")) {
					template.add(line);
				}
			}
			
			/* Add last */
			if ( ! keyword.isEmpty() && template != null )
				templates.put(keyword, template);
		}
	}
	
	public Set<String> keySet() {
		return templates.keySet();
	}
	
	public boolean isEmpty() {
		return templates.isEmpty();
	}
	
	public List<String> get(String key) {
		return templates.get(key);
	}
	
	public List<String> getOrDefault(String key) {
		return templates.getOrDefault(key, new ArrayList<>());
	}
	
	public void clear() {
		this.clear();
	}
	
	public void remove(String key) {
		this.remove(key);
	}
}
