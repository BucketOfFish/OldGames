import java.awt.Image;
import java.util.ArrayList;

class Item extends Spellcraft implements Cloneable {
	String name;
	ArrayList<String> description=new ArrayList<String>();
	Map location;
	int solidity,x,y,sizex=1,sizey=1;
	Image icon;
	public Item clone() {
		try {
			return (Item)super.clone();
		}
		catch(Exception e) {
			throw new InternalError(e.toString());
		}
	}
	Item(String name,int solidity,int sizex,int sizey) {
		this.name=name;
		this.solidity=solidity;
		this.sizex=sizex;
		this.sizey=sizey;
		icon=getImage("Items/"+name+".gif");
	}
	public void setsize(int sizex,int sizey) {
		this.sizex=sizex;
		this.sizey=sizey;
	}
}