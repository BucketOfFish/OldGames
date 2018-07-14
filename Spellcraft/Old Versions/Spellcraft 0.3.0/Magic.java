import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;

class Magic extends Spellcraft implements Cloneable {
	String name;
	ArrayList<String> description=new ArrayList<String>();
	Character caster;
	Image icon;
	Color color;
	int charge,speed;
	public Magic clone() {
		try {
			return (Magic)super.clone();
		}
		catch(Exception e) {
			throw new InternalError(e.toString());
		}
	}
	Magic(String name,String image,String description) {
		this.name=name;
		icon=getImage(image,"Magic");
		this.description=cut(description,166);
	}
	public void cast(Creature caster,int x,int y) {
/*		Ground terrain=null;
		ArrayList<Creature> targets=new ArrayList<Creature>();
		ArrayList<Item> Itemtargets=new ArrayList<Item>();
		try{
//			terrain=location.terrain[x][y];
		}
		catch(ArrayIndexOutOfBoundsException e) {}
		for(int count=0;count<location.population.size();count++) {
			Creature me=location.population.get(count);
			if(me.x*20<x&&me.x*20+20>x&&me.y*15<y&&me.y*15+15>y)
				targets.add(me);
		}
		for(int count=0;count<location.stock.size();count++) {
			Item it=location.stock.get(count);
			if(it.leftx()<x&&it.rightx()>x&&it.upy()<y&&it.downy()>y)
				Itemtargets.add(it);
		}
/*		if(spell==heal)
			caster.recover(10);
		else if(spell==shock) {
			for(int count=0;count<targets.size();count++)
				targets.get(count).damage(5);
		}
		else if(spell==zap) {
			for(int count=0;count<targets.size();count++)
				targets.get(count).damage(10);
		}
		else if(spell==freeze) {
			if(terrain.ID==1)
				terrain.set(4);
			for(int count=0;count<targets.size();count++)
				targets.get(count).frozen+=200;
		}
		else if(spell==unlock) {
			for(int count=0;count<Itemtargets.size();count++)
				if(Itemtargets.get(count).name.equals("lock"))
					location.remove(Itemtargets.get(count));
		}
		else if(spell==kill) {
			for(int count=0;count<targets.size();count++)
				targets.get(count).damage(targets.get(count).HP);
		}*/
	}
}