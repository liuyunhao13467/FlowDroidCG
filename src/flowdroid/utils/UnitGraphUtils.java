package flowdroid.utils;

import java.util.Iterator;
import java.util.Set;

import soot.Unit;
import soot.toolkits.graph.UnitGraph;

public class UnitGraphUtils {

	
	
	public static Unit getHead(Set<Unit> unitsNeeded, UnitGraph ug) {
		// TODO ��һ�����⣬�����ж��heads.�ȼ���ֻ��һ����
		Iterator<Unit> unitIt = unitsNeeded.iterator();
		Unit tmp = null;
		while (unitIt.hasNext()) {
			tmp = unitIt.next();
			if (ug.getPredsOf(tmp) == null || ug.getPredsOf(tmp).size() == 0) {
				return tmp;// ������Щ���⣬�����ж��ͷ��������ֻ������һ����
			}
		}
		return null;
	}
	
	/*
	 * ��һ��unitGraph�е�����unit֮��ı�ȥ���� �� src ---> tgt ��
	 */
	public static void dropEdge(Unit src, Unit tgt, UnitGraph ug) {
		ug.getSuccsOf(src).remove(tgt);
		ug.getPredsOf(tgt).remove(src);

	}
	
	/*
	 * ��unitGraph��ɾ��ɾ��ĳ����㡣
	 */
	public static void dropUnit(UnitGraph ug, Unit unitToDrop) {
		//TODO ɾ���������ס�
		for (Unit pre : ug.getPredsOf(unitToDrop)) {
			ug.getSuccsOf(pre).addAll(ug.getSuccsOf(unitToDrop));
			ug.getSuccsOf(pre).remove(unitToDrop);
		}
		for (Unit succ : ug.getSuccsOf(unitToDrop)) {
			ug.getPredsOf(succ).addAll(ug.getPredsOf(unitToDrop));
			ug.getPredsOf(succ).remove(unitToDrop);
		}
	}
	
	
	
}
