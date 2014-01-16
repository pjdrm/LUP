package l2f.utils.collections;

import java.util.*;

public class SortableMap {
	
	public enum ORDER {
		ASCENDING {
			@Override public <K, V extends Comparable<? super V>> void sortByValue(List<Map.Entry<K, V>> list) {
				Collections.sort(list, new Comparator<Map.Entry<K, V>>()
				        {
				            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
				            {
				                return (o1.getValue()).compareTo(o2.getValue());
				            }
				        });
			}
		},
		DESCENDING {
			@Override public <K, V extends Comparable<? super V>> void sortByValue(List<Map.Entry<K, V>> list) {
				Collections.sort(list, new Comparator<Map.Entry<K, V>>()
				        {
				            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
				            {
				                return (o2.getValue()).compareTo(o1.getValue());
				            }
				        });
			}
		};
		
		public abstract <K, V extends Comparable<? super V>> void sortByValue(List<Map.Entry<K, V>> list);
	}
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sort(Map<K, V> map, ORDER order) {
		List<Map.Entry<K, V>> list =
            new LinkedList<Map.Entry<K, V>>(map.entrySet());
		
		order.sortByValue(list);
		
		Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
	}
	
}

