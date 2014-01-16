package l2f.interpretation.classification;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class QuestionCategory {

	public static Vector<String> categories = new Vector<String>();

	public static void addCategory(String category){
		if(!categories.contains(category)){
			categories.add(category);
		}

	}

	public static String getCoarseCategory(String fine) {
		String coarse = fine;
		if (fine.indexOf('_') != -1) {
			coarse = fine.substring(0, fine.indexOf('_'));
		}
		return coarse;
	}

	public static String[] toFineStringArray() {
		
		String[] cats = new String[categories.size()];
		for (int i = 0; i < categories.size(); i++) {
			cats[i] = categories.get(i);
		}
		return cats;
	}

	public static String[] toCoarseStringArray() {
		
		Set<String> cats = new HashSet<String>();
		for (String qc : categories) {
			String fine = qc.toString();
			String coarse = fine;
			if (fine.indexOf('_') != -1) {
				coarse = fine.substring(0, fine.indexOf('_'));
			}
			cats.add(coarse);
		}
		String[] coarse = new String[cats.size()];
		cats.toArray(coarse);
		return coarse;
	}
}
