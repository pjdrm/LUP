package l2f.corpus.processor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

public class NEModifier extends StringModifier implements Serializable{

	private static final long serialVersionUID = -2004871987724909637L;
	
	private double CHUNK_SCORE = 1.0;
	private MapDictionary<String> DICTIONARY = new MapDictionary<String>();
	private Map<String, List<String>> namedEntities = new HashMap<String, List<String>>();
	private ExactDictionaryChunker dictionaryChunkerTT;
	
	public NEModifier(String neFilePath){
		loadDictionary(neFilePath);		
	}

	
	private void loadDictionary(String neFilePath) {		
		String line = new String();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(neFilePath), "UTF-8"));
			while ((line=reader.readLine())!=null) {
				String aux[] = line.split("\t");
				DICTIONARY.addEntry(new DictionaryEntry<String>(aux[1],aux[0],CHUNK_SCORE));
			}
			reader.close();

		} catch (IOException e) { 
			e.printStackTrace();
			System.exit(1);
		}
		dictionaryChunkerTT = new ExactDictionaryChunker(DICTIONARY, IndoEuropeanTokenizerFactory.INSTANCE,true,false);
	}

	private void addNE(String entityCat, String entity) {
		if(namedEntities.get(entityCat) == null){
			List<String> entities = new ArrayList<String>();
			entities.add(entity);
			namedEntities.put(entityCat, entities);
		}
		else{
			namedEntities.get(entityCat).add(entity);
		}
	}

	@Override
	public String modify(String str) {
		namedEntities.clear();
		List<String> entities = new ArrayList<String>();
		Chunking chunking = dictionaryChunkerTT.chunk(str);
		for (Chunk chunk : chunking.chunkSet()) {
			int start = chunk.start();
			int end = chunk.end();
			entities.add(str.substring(start, end) + "%" + chunk.type());
		} 
		String entityCat = "";
		String entity = "";
		for(int i=0; i < entities.size(); i++){
			entityCat = "NE_" + entities.get(i).split("%")[1];
			entity = entities.get(i).split("%")[0];
			str = str.replace(entity, entityCat);
			addNE(entityCat, entity);
		}
		return str;

	}

	@Override
	public String getDescription() {
		return "NE Parser";
	}
	
	@Override
	public String getModifications(){
		String entities = "";
		for(String entityCat : namedEntities.keySet()){
			entities += "#" + entityCat + "-" + namedEntities.get(entityCat).toString() + "# ";
		}
		return entities;//.substring(0, entities.length() - 2);
	}
}


