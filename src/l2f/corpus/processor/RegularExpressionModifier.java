package l2f.corpus.processor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegularExpressionModifier extends StringModifier implements Serializable {
	private Map<String, List<String>> regExprCats = new LinkedHashMap<String, List<String>>();
	private Map<String, String> regExpressions = new LinkedHashMap<String, String>();
	
	public RegularExpressionModifier(String reFilePath){
		loadRegularExpressions(reFilePath);		
	}

	
	private void loadRegularExpressions(String neFilePath) {		
		String line = new String();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(neFilePath), "UTF-8"));
			while ((line=reader.readLine())!=null) {
				String aux[] = line.split("\t");
				//was like this but dont know why I put a " " char...
				//regExpressions.put(aux[1],aux[0]+" ");
				regExpressions.put(aux[1],aux[0]);
			}
			reader.close();

		} catch (IOException e) { 
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void addRECat(String entityCat, String entity) {
		if(regExprCats.get(entityCat) == null){
			List<String> entities = new ArrayList<String>();
			entities.add(entity);
			regExprCats.put(entityCat, entities);
		}
		else{
			regExprCats.get(entityCat).add(entity);
		}
	}

	@Override
	public String modify(String str) {
		regExprCats.clear();
		for (String regExpr : regExpressions.keySet()) {
			String regExprCat = regExpressions.get(regExpr);
			boolean gotMatch = false;
			Pattern p = Pattern.compile(regExpr);
			Matcher m = p.matcher(str);

			while (m.find()) {
				addRECat(regExprCat, m.group());
				gotMatch = true;
			}
			if(gotMatch){
				str = m.replaceAll(regExprCat);
			}
		} 
		
		return str.trim();

	}

	@Override
	public String getDescription() {
		return "RE Parser";
	}
	
	@Override
	public String getModifications(){
		String entities = "";
		for(String regExprCat : regExprCats.keySet()){
			entities += "#" + regExprCat + "-" + regExprCats.get(regExprCat).toString() + "# ";
		}
		return entities;//.substring(0, entities.length() - 2);
	}
	
	private static final long serialVersionUID = 6699814256992361319L;
}
