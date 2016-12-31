package flowdroid.utils.graphUtils.dotUtils;

import java.io.File;

public class GraphVizTools {

	public static void createDotGraph(String dotFormat,String fileName)
	{
	    GraphViz gv=new GraphViz();
	    gv.addln(gv.start_graph());
	    gv.add(dotFormat);
	    gv.addln(gv.end_graph());
//	    String type = "gif";
	    String type = "pdf";
	    gv.increaseDpi();
	    gv.decreaseDpi();
	    gv.decreaseDpi();
	    File out = new File("E:/"+fileName+"."+ type); 
	    gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );
	    System.out.println("done !!!");
	}
	
}
