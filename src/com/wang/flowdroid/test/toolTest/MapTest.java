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
		//1.��map�У�keyֵ��set����ʽ�洢�ţ��������ǿ��Եõ�keyֵ�ļ��ϡ�
		Set<Integer> dataSet = entities.keySet();
		//2.�������ǣ����Եõ����е�keyֵ����ͨ����������
		Iterator<Integer> it = dataSet.iterator();
		while(it.hasNext()){
			//3.�õ�keyֵ�ˣ�valueֵҲ����֪֮���ˡ�
			System.out.println(entities.get(it.next()));
		}
	}

}
