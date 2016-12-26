package flowdroid.test.toolTest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MapTest {
	public static Map<Integer,String> entities =new HashMap<Integer, String>();
	
	public static void main(String[] args) {
		entities.put(1, "hello");
		entities.put(2, "world");
		entities.put(3, "sunny");
		//1.在map中，key值以set的形式存储着，所以我们可以得到key值的集合。
		Set<Integer> dataSet = entities.keySet();
		//2.进而我们，可以得到所有的key值。（通过迭代器）
		Iterator<Integer> it = dataSet.iterator();
		while(it.hasNext()){
			//3.得到key值了，value值也就随之知道了。
			System.out.println(entities.get(it.next()));
		}
	}

}
