package ch.uzh.ifi.ce.cabne.helpers;

public interface Histories<Value> {
	public int getRef(Double price); //TODO avoid hardcode, split histories into histories and futures
	public int getnHistories();
	public Value[] getBelief(int ref,boolean won);
}
