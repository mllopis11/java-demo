package mike.test.oracle;

import java.io.IOException;
import java.util.List;

import mike.test.oracle.SQLLoader.SQLLoaderBuilder;
import mike.utilties.Print;

public class OracleSQLLoaderTest {

	public static void go()  {
		
		String configFile = "mapper-loader.cfg";
		String filename = "./runtime/files/4567_321_TPS_ACCPIVOT_IPM_1M_8675_5_20161216.csv";
		String source = "IPM";
		int fileId = 321;
		
		SQLLoaderTemplate templates = new SQLLoaderTemplate();
		
		try {
			templates.load(configFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Print.info("----- SQL*Loader Templates -----", source);
		templates.keySet().forEach( k -> { 
			Print.info("> Template: %s", k); 
			templates.get(k).forEach( l -> Print.info("- line: %s", l));
		});
		
		Print.info("----- SQL*Loader Templates -----", source);
		
		List<String> template = templates.getOrDefault("IPM"); 
		
		SQLLoader loader = SQLLoaderBuilder.build(s -> {
			s.setFileToLoad(filename);
			s.setControlFileTemplate(template);
			s.putParameter("file_id", fileId);
			s.putParameter("table", "TPS_IPM_STAGING");
			s.putParameter("separator", ",");
			s.putParameter("load_mode", "APPEND");
		});
		
		loader.getControlFile().forEach(l -> Print.info("> Line: %s", l));
		
		Print.info(loader.toString());
	}
}
