package com.sg.rdj.mapper.commons.jdbc.oracle;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.sg.rdj.mapper.commons.utilities.AppInfo;
import com.sg.rdj.mapper.commons.utilities.Dates;

public class SQLLoaderDefn {

	@FunctionalInterface
	public interface SQLLoaderSetter extends Consumer<SQLLoaderDefn> {}
		
	private List<String> template = new ArrayList<>();
	private Map<String, String> parameters = new HashMap<>();
	
	private final List<String> controlFile = new ArrayList<>();
	
	private Path fileToLoad;
	private String basename = "ldr_default";
	
	private String logDirectory = "";
	private String workDirectory = "";
	
	private SQLLoaderDefn() {}
	
	public void setControlFileTemplate(List<String> template) {
		this.template.addAll(template);
	}
	
	public void putParameter(String key, Object value) {
		String k = valueOf(key);
		
		if ( ! k.isEmpty() )
			parameters.put("{" + k + "}", String.valueOf(value));
	}
	
	public void putParameters(Map<String, Object> parameters) {
		parameters.keySet().forEach( key -> this.putParameter(key, parameters.get(key)));
	}
	
	public void setFileToLoad(String filename) {
		String value = this.valueOf(filename);
		if ( ! value.isEmpty() )
			this.setFileToLoad(Paths.get(value));
	}
	
	public void setFileToLoad(Path file) {
		if ( file != null )
			this.fileToLoad = file;
	}
	
	public Path getFileToLoad() {
		return this.fileToLoad; 
	}
	
	public void setLoaderBaseName(String basename) {
		this.basename = this.valueOf(basename);
	}
	
	public void setLogDirectory(String directory) {
		this.logDirectory = this.valueOf(directory);
		if ( this.workDirectory.isEmpty() )
			this.workDirectory = this.logDirectory;
	}
	
	public String getLogDirectory() {
		return this.logDirectory;
	}
	
	public void setWorkDirectory(String directory) {
		this.workDirectory = this.valueOf(directory);
		if ( this.logDirectory.isEmpty() )
			this.logDirectory = this.workDirectory;
	}
	
	public String getWorkDirectory() {
		return this.workDirectory;
	}
	
	public Path getLogFile() {
		return Paths.get(this.getLogDirectory(), this.basename + ".log");
	}
	
	public Path getBadFile() {
		return Paths.get(this.getLogDirectory(), this.basename + ".bad");
	}
	
	public Path getDiscardFile() {
		return Paths.get(this.getLogDirectory(), this.basename + ".dsc");
	}
	
	public Path getControlFile() {
		return Paths.get(this.getWorkDirectory(), this.basename + ".ctl");
	}
	
	public Path getParFile() {
		return Paths.get(this.getWorkDirectory(), this.basename + ".par");
	}
	
	public String getBasename() {
		return this.basename;
	}
	
	public List<String> getControlFileContent() {
		final List<String> controlFile = new ArrayList<>();
		
		template.forEach( l -> {		
			
			String line = l;

			for ( String key : parameters.keySet() ) {
				if ( line.contains(key) )
					line = line.replace(key, parameters.get(key));					
			}
			
			controlFile.add(line);
			
			if ( line.equals("LOAD DATA") ) {
				controlFile.add("INFILE '" + this.fileToLoad.toString() + "'");
				controlFile.add("BADFILE '" + this.getBadFile().toString() + "'");
				controlFile.add("DISCARDFILE '" + this.getDiscardFile().toString() + "'");
			}
		});
		
		return controlFile;
	}
	
	private String valueOf(String value) {
		return value != null ? value.trim() : "";
	}
	
	/**
	 * Build object
	 * 
	 * @return built object instance
	 */
	private SQLLoaderDefn build() {
		
		if ( this.getFileToLoad() == null )
			throw new IllegalStateException("SQLLoaderDefn: no such input file");
		
		if ( this.template.isEmpty() )
			throw new IllegalStateException("SQLLoaderDefn: no such template");
		
		String dateTime = Dates.dNow("yyyyMMdd_HHmmss");
		this.basename = String.format("%s_%s_%s", this.basename, dateTime, AppInfo.pid());
		
		if ( this.getLogDirectory().isEmpty() )
			this.setLogDirectory(this.getFileToLoad().getParent().toString());
			
		if ( this.getWorkDirectory().isEmpty() )
			this.setWorkDirectory(this.getFileToLoad().getParent().toString());
		
		if ( ! Files.isWritable(Paths.get(this.getLogDirectory())) )
			throw new IllegalStateException("SQLLoaderDefn: no such writable directory: " + this.getLogDirectory());
		
		if ( ! Files.isWritable(Paths.get(this.getWorkDirectory())) )
			throw new IllegalStateException("SQLLoaderDefn: no such writable directory: " + this.getWorkDirectory());
		
		/* *** Build Control File *** */
		template.forEach( l -> {		
			
			String line = l;

			for ( String key : parameters.keySet() ) {
				if ( line.contains(key) )
					line = line.replace(key, parameters.get(key));					
			}
			
			controlFile.add(line);
			
			if ( line.equals("LOAD DATA") ) {
				controlFile.add("INFILE '" + this.fileToLoad.toString() + "'");
				controlFile.add("BADFILE '" + this.getBadFile().toString() + "'");
				controlFile.add("DISCARDFILE '" + this.getDiscardFile().toString() + "'");
			}
		});
		
		controlFile.stream()
			.filter(l -> l.contains("{") || l.contains("}"))
			.forEach(l -> { throw new IllegalStateException("SQLLoaderDefn: remaining paramaters for line: " + l); });
			
		return this;
	}
	
	/**
	 * Object builder
	 */
	public static class SQLLoaderDefnBuilder {
		
		public static SQLLoaderDefn build(SQLLoaderSetter... setters) {
			final SQLLoaderDefn loader = new SQLLoaderDefn();
			Stream.of(setters).forEach(s -> s.accept(loader));
			return loader.build();
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SQLLoaderDefn [fileToLoad=").append(fileToLoad)
				.append(", basename=").append(basename)
				.append(", logDir=").append(logDirectory)
				.append(", workDir=").append(workDirectory).append("]");
		return builder.toString();
	}
}
