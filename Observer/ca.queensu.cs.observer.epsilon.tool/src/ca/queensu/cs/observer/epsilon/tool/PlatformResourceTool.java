package ca.queensu.cs.observer.epsilon.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.epsilon.eol.tools.AbstractTool;

public class PlatformResourceTool extends AbstractTool {

	protected String name;
	
	public void setName(String name) {
		 this.name = name;
	}
  
	public String getName() {
		return name;
	}
	
	public String getContents(String path) {
		String content = "";
		try {
			URL url = new URL(path);
			System.out.println(url);
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

	        String inputLine;
	        while ((inputLine = in.readLine()) != null)
	        	content += inputLine + "\n";
	        in.close();			        
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}
}
