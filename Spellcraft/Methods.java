import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;
@SuppressWarnings("serial")
public class Methods extends Applet {
	public static ArrayList<String> cut(String input,int length) {
		StringTokenizer tokens=new StringTokenizer(input);
		ArrayList<String> outputs=new ArrayList<String>();
		String concat="";
/*		while(metrics.stringWidth(input)>length) {
			String line=concat;
			try {
				while(metrics.stringWidth(line.concat(concat=tokens.nextToken()+" "))<length)
					line=line.concat(concat);
			}
			catch(Exception e) {}
			outputs.add(line);
			input=input.substring(line.length());
		}*/
		outputs.add(input);
//		String[] output=new String[outputs.size()];
//		for(int count=0;count<outputs.size();count++)
//			output[count]=outputs.get(count);
		return outputs;
	}
	Image crop(Image icon,int x,int y,int width,int height) {
		return createImage((ImageProducer) new FilteredImageSource(icon.getSource(),new CropImageFilter(x,y,width,height)));
	}
	Image getImage(String image,String source) {
		MediaTracker tracker=new MediaTracker(this);
		Image output;
		if(source!=null)
			tracker.addImage(output=getImage(getCodeBase(),"Images/"+source+"/"+image),0);
		else
			tracker.addImage(output=getImage(getCodeBase(),"Images/"+image),0);
		try {
			tracker.waitForID(0);
		}
		catch (InterruptedException e) {}
		return output;
	}
	int change(int input,int change,int lower,int higher) {
		int output=input+change;
		if(output<lower)
			output=higher;
		if(output>higher)
			output=lower;
		return output;
	}
}