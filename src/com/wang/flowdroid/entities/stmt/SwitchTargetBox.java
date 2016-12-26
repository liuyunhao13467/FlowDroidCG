package flowdroid.entities.stmt;

import java.util.List;

import soot.Unit;
/**
 * ����ĵ�����࣬��Ҫ����Ϊ�޷�����JTableSwitchStmt�е����ݣ�����Ҳ�ǲ���ȫ�ġ�
 * ��������JTableSwitchStmt֮������˸���֮��ĵ��µĵ����ݵı�������������Щ�ң�
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
