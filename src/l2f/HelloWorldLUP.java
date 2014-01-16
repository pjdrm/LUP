package l2f;

import java.io.IOException;

import l2f.evaluator.QCEAnswer;
import l2f.evaluator.QuestionClassifierEvaluator;

public class HelloWorldLUP {
	public static void main(String[] args){
		System.out.println("Creating Classifier...");
		QuestionClassifierEvaluator qce = ClassifierApp.getDeployedQCE(new String[]{"cinema"});
		String question = "Quem foi o argumentista de To Kill A Mockingbird";
		System.out.println("Test Question: " + question);
		QCEAnswer answer = qce.answerWithQCEAnswer(question);
		System.out.println("Answer:\n" + answer.getStringPossibleAnswers() + "\n" + answer.getRecognisedNE());
	}
}
