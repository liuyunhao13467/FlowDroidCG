package flowdroid.test.toolTest;

import java.io.File;
import java.io.IOException;

import flowdroid.tools.dotGraphTools.GraphViz;
import flowdroid.tools.dotGraphTools.Method2Graph;

public class GraphVizBasicTest {

	public static void main(String[] args) {
		 String dotFormat="1->2;1->3;1->4;4->5;4->6;6->7;5->7;3->8;3->6;8->7;2->8;2->5;";
	        Method2Graph.createDotGraph(dotFormat, "DotGraph");
	}


}
