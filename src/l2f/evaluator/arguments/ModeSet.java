package l2f.evaluator.arguments;

public enum ModeSet {
		DEVELOP("develop"),
		RETEST("retest"),
		DEPLOY("deploy"),
		CrossValidation("cross validation"),
		ReCrossValidate("re-cross-validate"),
		TEST("test"),
		OOD("out of domain");
		
		
		private String mode;
		ModeSet(String mode){
			this.mode = mode;
		}
		
		public String mode(){return mode;}
}
