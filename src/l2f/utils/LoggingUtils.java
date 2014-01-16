package l2f.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.EnumMap;

/**
 * Class that represents the logging system, can be extended to support other loggers assuring the 
 * construction of identical messages
 * @author Sergio Curto
 *
 */
public class LoggingUtils {
	public enum LoggingLevel {
		/**
		 * Denotes an error that occurred during the execution of the application
		 */
		CRITICAL(System.err),
		/**
		 * Debug information that should be stored to keep track of the execution of the application
		 */
		INFORMATIONAL(System.out),
		/**
		 * Application information indicating what is currently being done
		 */
		STATE(System.out);

		private final PrintStream defaultPrintStream;
		
		private LoggingLevel(PrintStream ps) {
			this.defaultPrintStream = ps;
		}
		
		public PrintStream getDefaultOuputStream() {
			return defaultPrintStream;
		}
	}
	
	EnumMap<LoggingLevel, PrintStream> streamMaps = new EnumMap<LoggingUtils.LoggingLevel, PrintStream>(LoggingLevel.class);
	
	static private LoggingUtils instance = null;
	
	public static LoggingUtils getInstance() {
		if(instance == null) {
			instance = new LoggingUtils();
		}
		return instance;
	}
	
	public static void setInstance(LoggingUtils newInstance) {
		instance = newInstance;
	}
	
	protected LoggingUtils() {
		for(LoggingLevel logLvl : LoggingLevel.values()) {
			streamMaps.put(logLvl, logLvl.getDefaultOuputStream());
		}
	}
	
	public void log(@SuppressWarnings("rawtypes") Class c, LoggingLevel level, String message) {
		PrintStream ps = streamMaps.get(level);
		
		ps.println(generateMessage(c, level, message));
	}
	
	private String generateMessage(@SuppressWarnings("rawtypes") Class c, LoggingLevel level, String message) {
		String className = c.getName();
		String packageName = c.getPackage().getName();
		
		return level +": "+ packageName+"."+className +": "+ message;
	}

	public void setLevelStream(LoggingLevel level, PrintStream ps){
		streamMaps.put(level, ps);
	}
	
	public void setLevelStream(LoggingLevel level, OutputStream out){
		streamMaps.put(level, new PrintStream(out));
	}
	
	public void setLevelStream(LoggingLevel level, File file, String encoding) throws FileNotFoundException, UnsupportedEncodingException{
		streamMaps.put(level, new PrintStream(file, encoding));
	}
	
	public void setAllLevelsStream(PrintStream ps){
		for(LoggingLevel lvl : LoggingLevel.values()){
			streamMaps.put(lvl, ps);
		}
	}
	
	public void setAllLevelsStream(LoggingLevel level, OutputStream out){
		for(LoggingLevel lvl : LoggingLevel.values()){
			streamMaps.put(lvl, new PrintStream(out));
		}
	}
	
	public void setAllLevelsStream(LoggingLevel level, File file, String encoding) throws FileNotFoundException, UnsupportedEncodingException{
		for(LoggingLevel lvl : LoggingLevel.values()){
			streamMaps.put(lvl, new PrintStream(file, encoding));
		}
	}
}
