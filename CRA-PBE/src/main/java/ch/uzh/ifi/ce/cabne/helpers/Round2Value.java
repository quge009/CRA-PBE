package ch.uzh.ifi.ce.cabne.helpers;

public class Round2Value {
public int histRef;
public Double cost;
public boolean won;

//for design simplicity we model histories as part of a bidding agents value
public Round2Value(int histRef, boolean won, Double cost) {
	this.histRef=histRef;
	this.won=won;
	this.cost=cost;
}
}
