package l2f.config;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ResourceLoader {
	public InputStream getResource(String path){
		FileInputStream fis = null;
		
		try {
			fis = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return fis;
	}
	
	public boolean directFileIOAllowed(){
		return true;
	}
	
	protected ResourceLoader() {
		
	}
	
	static private ResourceLoader rl = null;
	static public ResourceLoader getResourceLoader() {
		if(rl == null) {
			rl = new ResourceLoader();
		}
		
		return rl;
	}
	
	static public void setResourceLoader(ResourceLoader rl) {
		ResourceLoader.rl = rl;
	}
}
