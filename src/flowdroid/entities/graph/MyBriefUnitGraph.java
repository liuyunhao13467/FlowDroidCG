package flowdroid.entities.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.Unit;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;


/*
 * 1.没有多余的头部，只有一个头。
 * 2.没有环路的存在的UnitGraph。
 */
public class MyBriefUnitGraph extends BriefUnitGraph {
	enum VisitType {
		VISIT_NONE, VISITING, VISITED
	};

	protected boolean isDegugging = false;

	public MyBriefUnitGraph(Body body) {
		super(body);
		removeBogusHeads();
		removeAllCircles(this);
		buildHeadsAndTails();
	}

	protected void removeBogusHeads() {
		System.out.println("removeBogusHeads start ~~");
		Unit trueHead = body.getUnits().getFirst();

		while (this.getHeads().size() > 1) {
			for (Unit head : this.getHeads()) {
				if (trueHead == head)
					continue;

				this.unitToPreds.remove(head);
				if (this.unitToSuccs.get(head) != null) {
					for (Unit succ : this.unitToSuccs.get(head)) {
						List<Unit> tobeRemoved = new ArrayList<Unit>();
						List<Unit> predOfSuccs = this.unitToPreds.get(succ);
						if (predOfSuccs != null) {
							for (Unit pred : predOfSuccs) {
								if (pred == head)
									tobeRemoved.add(pred);

							}
							predOfSuccs.removeAll(tobeRemoved);
						}
					}
				}

				this.unitToSuccs.remove(head);

				if (unitChain.contains(head))
					unitChain.remove(head);
			}

			this.buildHeadsAndTails();
		}

		System.out.println("removeBogusHeads end ~~ head size : " + heads.size());

	}

	protected UnitGraph removeAllCircles(UnitGraph ug) {
		Map<Unit, VisitType> visitInfo = new HashMap<>();

		System.out.println("removeAllCircles start ~~");
		// 结点标记 初始化。
		Iterator<Unit> ugIt = ug.iterator();
		while (ugIt.hasNext()) {
			Unit u = ugIt.next();
			visitInfo.put(u, VisitType.VISIT_NONE);
		}
		Unit head = getHead(ug);
		System.out.println("before dfs dropCircle ~~");

		dropCircle(head, ug, visitInfo);

		System.out.println("after dfs dropCircle ~~");
		System.out.println("removeAllCircles end ~~");
		return ug;
	}

	private void dropCircle(Unit currentUnit, UnitGraph ug, Map<Unit, VisitType> visitInfo) {
		if (currentUnit == null) {
			return;
		}
		visitInfo.put(currentUnit, VisitType.VISITING);

		Iterator<Unit> childIt = ug.getSuccsOf(currentUnit).iterator();
		while (childIt.hasNext()) {
			Unit child = childIt.next();
			if (visitInfo.get(child) == VisitType.VISIT_NONE) {
				// 递归处理
				dropCircle(child, ug, visitInfo);
				continue;
			} else if (visitInfo.get(child) == VisitType.VISITING) {
				// 【关键】删除边，并且不处理。
				ug.getPredsOf(child).remove(currentUnit);
				childIt.remove();
				continue;
			} else if (visitInfo.get(child) == VisitType.VISITED) {
				// do nothing.
				continue;
			}
		}

		visitInfo.put(currentUnit, VisitType.VISITED);
	}

	private Unit getHead(UnitGraph ug) {
		if (ug.getHeads().size() > 1) {
			try {
				throw new Exception(ug.getBody().getMethod().getName() + "不是拥有一个头");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		Iterator<Unit> ugIt = ug.getHeads().iterator();
		while (ugIt.hasNext()) {
			return ugIt.next();
		}
		return null;
	}
}
