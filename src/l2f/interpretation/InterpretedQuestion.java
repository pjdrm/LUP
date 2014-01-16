package l2f.interpretation;


public class InterpretedQuestion implements Comparable<InterpretedQuestion> {

    private int id = 0;
    private static int numberInterpretedQuestions = 0;
    private final AnalyzedQuestion analyzedQuestion;
    private final String questionCategory;
    private String predictedQuestionCategory;

    public InterpretedQuestion(AnalyzedQuestion analyzedQuestion,
            String questionCategory) {
        numberInterpretedQuestions++;
        this.analyzedQuestion = analyzedQuestion;
        this.questionCategory = questionCategory;
        this.predictedQuestionCategory = "";
        this.id = numberInterpretedQuestions;

    }

    public int getId() {
        return id;
    }

    public AnalyzedQuestion getAnalyzedQuestion() {
        return analyzedQuestion;
    }

    public String getQuestionCategory() {
        return questionCategory;
    }

    public String getPredictedQuestionCategory() {
        return predictedQuestionCategory;
    }

    public void setPredictedQuestionCategory(String predictedQuestionCategory) {
        this.predictedQuestionCategory = predictedQuestionCategory;
    }

    
    

    public static void setNumberInterpretedQuestions(int numberInterpretedQuestions) {
        InterpretedQuestion.numberInterpretedQuestions = numberInterpretedQuestions;
    }

    

    public int compareTo(InterpretedQuestion iq) {
        if (iq.getId() > this.getId()) {
            return -1;
        }
        return 1;
    }
}
