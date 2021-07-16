package com.sg.rdj.mapper.commons.jdbc.oracle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.slf4j.Logger;

import com.sg.rdj.mapper.commons.jdbc.DatabaseConnector;
import com.sg.rdj.mapper.commons.utilities.Dates;
import com.sg.rdj.mapper.commons.utilities.Helpers;

public class SQLLoader {

	@FunctionalInterface
	public interface SQLLoaderSetter extends Consumer<SQLLoader> {}
	
	private static final List<String> Commands = new ArrayList<>();
	private static final String ShellExtension;
	
	private static Logger logger = null;
	private static String tName = "";
	
	static {
		if (Helpers.isWindows()) {
			ShellExtension = "bat";
			Commands.add("@echo OFF");
			Commands.add("REM SQL*Loader script generated at " + Dates.zNow());
			Commands.add("SET ORACLE_HOME={oracle_home}");
			Commands.add("SET TNS_ADMIN={tns_admin}");
			Commands.add("SET PATH=%ORACLE_HOME%\\bin");
			Commands.add("echo TRACE: Resolving TNS name: {tns_name}");
			Commands.add("tnsping {tns_name}");
			Commands.add("echo TRACE: SQL*Loader starting ...");
			Commands.add("cd {exec_directory}");
			Commands.add("sqlldr parfile={parfile} control={control} log={logfile}");
			Commands.add("exit %ERRORLEVEL%");
		} else {
			ShellExtension = "sh";
			Commands.add("#!/bin/bash");
			Commands.add("# SQL*Loader script generated at " + Dates.zNow());
			Commands.add("exec &2 > &1");
			Commands.add("ORACLE_HOME={oracle_home}");
			Commands.add("TNS_ADMIN={tns_admin}");
			Commands.add("PATH=${ORACLE_HOME}\\bin:${PATH}");
			Commands.add("echo TRACE: SQL*Loader starting ...");
			Commands.add("cd ${exec_directory}");
			Commands.add("sqlldr parfile=${parfile} control=${control} log=${logfile}");
			Commands.add("exit $?");
		}
	}
	
	private String username = "";
	private String password = "";
	private String tnsName = "";
	
	private String oracleHome = "";
	private String tnsAdmin = "";
	
	private SQLLoader() {}
	
	public void setLogger(Logger logger, String... threadName) {
		SQLLoader.logger = logger;
		SQLLoader.tName = threadName.length > 0 ? threadName[0] : "[Loader]";
	}
	
	public void setOracleHome(final String oracleHome) {
		this.oracleHome = oracleHome != null ? oracleHome.trim() : "";
		this.setTnsAdmin(oracleHome + "\\network\\admin");
	}
	
	public void setTnsAdmin(final String tnsAdmin) {
		this.tnsAdmin = tnsAdmin != null ? tnsAdmin.trim() : "";
	}
	
	public void setCredentials(String username, String password, String tnsName) {
		this.username = username;
		this.password = password;
		this.tnsName = tnsName;
	}
	
	public void setConnector(DatabaseConnector connector) {
		this.username = connector.getUser();
		this.password = connector.getPass();
		this.tnsName = connector.getDatabase();
	}
	
	public void start(SQLLoaderDefn loader) throws Exception  {
			
		final List<String> fileContent = new ArrayList<>();
		
		/* *** Generate control and parameters files *** */
		this.createFile(loader.getControlFile(), loader.getControlFileContent());
		
		fileContent.clear();
		fileContent.add(String.format("USERID=%s/%s@%s", username, password, tnsName));
		this.createFile(loader.getParFile(), fileContent);
		
		/* *** Generate SQL*Loader script *** */
		String scriptName = String.format("%s.%s", loader.getBasename(), ShellExtension);
		Path scriptFile = Paths.get(loader.getWorkDirectory(), scriptName);
		
		fileContent.clear();
		
		Commands.forEach( c -> {
			String line = c.replace("${parfile}", loader.getParFile().toString())
							.replace("${control}", loader.getControlFile().toString())
							.replace("${logfile}", loader.getLogFile().toString())
							.replace("${oracle_home}", this.oracleHome)
							.replace("${tns_admin}", this.tnsAdmin)
							.replace("${tns_name}", this.tnsName)
							.replace("${exec_directory}", Helpers.USER_DIR);
			
			fileContent.add(line);
		});
		
		this.createFile(scriptFile, fileContent);
		this.setExecutable(scriptFile);
		
		/* *** Execute Script *** */
		int rc = 1;
		try {
			rc = this.execute(scriptFile, loader.getLogFile());
		} catch (IOException | InterruptedException ex) {
			deleteFileIfExists(loader.getParFile());
			throw ex;
		}
		
		if ( rc != 0 )
			throw new IOException("loader aborted with return code: " + rc + "(see logfile)");
	}
	
	private int execute(Path scriptFile, Path logFile) throws IOException, InterruptedException {
		int rc = 1;
		
		logger.info("{} execute: {} (location: {})", tName, scriptFile.getFileName(), scriptFile.getParent());
		
		ProcessBuilder process = new ProcessBuilder(scriptFile.toString());
		Process exec = process.start();
		
		/* *** Read stdout *** */
		try ( Scanner sc = new Scanner(exec.getInputStream()) ) {
			
			while ( sc.hasNextLine() ) {
				String line = sc.nextLine().trim();
				
				if (line.contains("Load completed") 
						|| line.startsWith("ORA-") || line.contains("ERROR") || line.startsWith("TRACE: ") ) {
					logger.info("{} exec> {}", tName, line.replaceFirst("TRACE: ", ""));
				}
			}
		}
		
		exec.waitFor();
		rc = exec.exitValue();
		
		/* *** Check log file *** */
		if ( Files.exists(logFile) ) {
			
			try ( Stream<String> reader = Files.lines(logFile) ) {
				reader.forEach( l -> {
					if ( l.contains("Load completed") ) {
						logger.info("{} {}", tName, l); 
					} else if ( l.startsWith("ORA-") || l.contains("ERROR") ) {
						logger.error("{} {}", tName, l); 
					}
				});
			} catch (IOException ioe) {
				logger.warn("{} parse logfile: {} (cause: {})", tName, logFile, Helpers.getExceptionMessage(ioe)); 
			}
		}
		
		return rc;
	}
	
	private void createFile(Path file, List<String> content, StandardOpenOption... options) throws IOException {

		try {
			logger.debug("{} create file: {}", tName, file);
			Files.write(file, content, options);
		} catch (IOException ex) {
			throw new IOException("SQLLoader: create file: " + file + " (cause: " + Helpers.getExceptionMessage(ex) + ")");
		}
	}
	
	private void deleteFileIfExists(Path file) {
		try { Files.deleteIfExists(file); } 
		catch (IOException ex) {
			logger.warn("{} file not deleted: {} (cause: {})", tName, file, Helpers.getExceptionMessage(ex));
		}
	}
	
	private void setExecutable(Path file) throws IOException {
		
		if ( Helpers.isUnix() ) {
			final Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr-xr--");
			try {
				Files.setPosixFilePermissions(file, perms);
			} catch (IOException ex) {
				throw new IOException("SQLLoader: set permissions: " + file + " (cause: " + Helpers.getExceptionMessage(ex) + ")");
			}
		}
	}
	
	private SQLLoader build() {
		
		if ( this.username.isEmpty() || this.password.isEmpty() || this.tnsName.isEmpty() )
			throw new IllegalStateException("SQLLoader: invalid credentials");
		
		if ( this.oracleHome.isEmpty() )
			throw new IllegalStateException("SQLLoader: no such ORACLE_HOME value");
		
		if ( ! Files.exists(Paths.get(this.oracleHome)))
			throw new IllegalStateException("SQLLoader: no such ORACLE_HOME directory: " + this.oracleHome);
		
		return this;
	}
	
	/**
	 * Builder (constructor)
	 */
	public static class SQLLoaderBuilder {
		
		public static SQLLoader build(SQLLoaderSetter... setters) {
			final SQLLoader loader = new SQLLoader();
			Stream.of(setters).forEach(s -> s.accept(loader));
			return loader.build();
		}
	}
}
