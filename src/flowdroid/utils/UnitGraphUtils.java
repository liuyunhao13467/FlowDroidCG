package flowdroid.utils;

import java.util.Iterator;
import java.util.Set;

import soot.Unit;
import soot.toolkits.graph.UnitGraph;

public class UnitGraphUtils {

	
	
	public static Unit getHead(Set<Unit> unitsNeeded, UnitGraph ug) {
		// TODO 有一点问题，可能有多个heads.先假设只有一个。
		Iterator<Unit> unitIt = unitsNeeded.iterator();
		Unit tmp = null;
		while (unitIt.hasNext()) {
			tmp = unitIt.next();
			if (ug.getPredsOf(tmp) == null || ug.getPredsOf(tmp).size() == 0) {
				return tmp;// 可能有些问题，可能有多个头部，这里只考虑了一个。
			}
		}
		return null;
	}
	
	/*
	 * 将一个unitGraph中的两个unit之间的边去掉。 （ src ---> tgt ）
	 */
	public static void dropEdge(Unit src, Unit tgt, UnitGraph ug) {
		ug.getSuccsOf(src).remove(tgt);
		ug.getPredsOf(tgt).remove(src);

	}
	
	/*
	 * 在unitGraph中删除删除某个结点。
	 */
	public static void dropUnit(UnitGraph ug, Unit unitToDrop) {
		//TODO 删除不够彻底。
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
