package flowdroid.entities.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import flowdroid.utils.graphUtils.dotUtils.UnitGraph2Dot;
import soot.Unit;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;
import soot.util.HashChain;

public class UnitGraphForTopology {

	UnitGraph ug;
	protected Map<Unit, List<Unit>> unitToSuccs;
	protected Map<Unit, List<Unit>> unitToPreds;
	protected Chain<Unit> unitChain;
	
	protected Queue<Unit> topologyOder;

	public UnitGraphForTopology(UnitGraph ug) {
		System.out.println("topology start ~~");

		this.ug = ug;
		unitToSuccs = new HashMap<Unit, List<Unit>>();
		unitToPreds = new HashMap<Unit, List<Unit>>();
		unitChain = new HashChain<>();
		topologyOder = new LinkedList<>();
		
		copyInfo(ug);
		setTopologicalOrder(); //这样写，有点太死。
		System.out.println("topology end ~~");
	}
	public Queue<Unit> getTopologyOrder(){
		return topologyOder;
	}

	private void copyInfo(UnitGraph ug) {
		Iterator<Unit> unitIt = ug.iterator();
		while (unitIt.hasNext()) {
			Unit u = unitIt.next();

			unitChain.add(u);
			List<Unit> pre = new LinkedList<>();
			List<Unit> succ = new LinkedList<>();

			pre.addAll(ug.getPredsOf(u));
			succ.addAll(ug.getSuccsOf(u));

			unitToPreds.put(u, pre);
			unitToSuccs.put(u, succ);

		}
	}

	private List<Unit> getPredsOf(Unit u) {
		List<Unit> l = unitToPreds.get(u);
		if (l == null)
			return Collections.emptyList();

		return l;
	}

	private List<Unit> getSuccsOf(Unit u) {
		List<Unit> l = unitToSuccs.get(u);
		if (l == null)
			return Collections.emptyList();

		return l;
	}

	private int size() {
		return unitChain.size();
	}

	private void setTopologicalOrder(){
		boolean isChanged = false;
		System.out.println("setTopologicalOrder start ~~");
		while(unitChain.size() != 0){//TODO 出现死循环
			isChanged = false;
			Iterator<Unit> unitIt = unitChain.iterator();
			while(unitIt.hasNext()){
				Unit tmp = unitIt.next();
				if(getPredsOf(tmp) == null || getPredsOf(tmp).size() == 0 ){
					isChanged = true;
					//加入到记录中。 
					topologyOder.add(tmp);
					//删除与之相关联的边。
					dropEdgeRelateToUnit(tmp);
					//从迭代器中删除该节点。
					unitIt.remove();
				}
			}
			
			if(!isChanged){
				System.out.println("出问题的 数据： " + ug.getBody().getMethod());
				UnitGraph2Dot.drawMethodUnitGraph(ug);
				System.exit(0);
				
			}
		}
		System.out.println("setTopologicalOrder end ~~");
	}
	
	private void dropEdgeRelateToUnit(Unit unitToDrop){
		for (Unit succ : getSuccsOf(unitToDrop)) {
			getPredsOf(succ).remove(unitToDrop);
		}
		getSuccsOf(unitToDrop).clear();
	} 
}
