package l2f.evaluator.arguments;

public class SVMArgumentsException extends Exception{
	private static final long serialVersionUID = 6981078652445272818L;
	
	private String message;
	public SVMArgumentsException(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return "ERROR: Invalid QCE arguments\n" + message;
	}
}
