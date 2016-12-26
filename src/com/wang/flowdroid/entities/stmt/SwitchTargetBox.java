package flowdroid.entities.stmt;

import java.util.List;

import soot.Unit;
/**
 * 下面的的这个类，主要是因为无法更改JTableSwitchStmt中的内容，而且也是不安全的。
 * 所以我在JTableSwitchStmt之外进行了更改之后的的新的的数据的保留。（不过有些乱）
 * @author wang
 *
 */
public class SwitchTargetBox {
protected Unit switchTable;	
protected Unit defultTarget;
//protected List<Unit> targets;
protected Unit[] targets;
int lowIndex;
int highIndex;

public SwitchTargetBox(Unit switchTable){
	this.switchTable = switchTable;
}


public Unit getTarget(int index)
{
    return targets[index];
}


 


public int getLowIndex() {
	return lowIndex;
}

public void setLowIndex(int lowIndex) {
	this.lowIndex = lowIndex;
}

public int getHighIndex() {
	return highIndex;
}

public void setHighIndex(int highIndex) {
	this.highIndex = highIndex;
}


}
