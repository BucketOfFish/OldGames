class Ground extends Spellcraft implements Cloneable {
	String name;
	boolean passable;
	Creature occupant;
	Item item;
	public Ground clone() {
		try {
			return (Ground)super.clone();
		}
		catch(Exception e) {
			throw new InternalError(e.toString());
		}
	}
	Ground(String name,boolean passable) {
		this.name=name;
		this.passable=passable;
//		this.image=crop(image,0,ID*Spellcraft.FLOORHEIGHT,Spellcraft.FLOORWIDTH,Spellcraft.FLOORHEIGHT);
	}
}