package ch.uzh.ifi.ce.cabne.pointwiseBR.updateRule;


/*
 * This rule tells us how to update the current bid at a specific value v_i, e.g. for piecewise linear strategies
 */
public interface UpdateRule<Bid> {

	public Bid update(Bid oldbid, Bid newbid, double oldutility, double newutility);
}
