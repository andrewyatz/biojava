/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 */

package org.biojava.bio.symbol;

import java.util.*;


import org.biojava.bio.*;
import org.biojava.utils.*;



/**
 * Circular view onto an underlying Location instance. If the location overlaps
 * the origin of the sequence the underlying location will be a CompoundLocation
 * Note that in this case isContiguous() will return false. This behaviour is
 * desirable for proper treatment of the location with LocationTools.
 * To find if a location overlaps the origin use the overlapsOrigin() method
 * <p>
 * Note also that as a location that overlaps the origin is a compound location it's
 * min will be 1 and its max will be length (by default). In these cases it is imperative to
 * use the block iterator if you wish to know the 'true' min and max (bearing in mind that
 * there is no logical value for a min or max on a circular sequence).
 * </p>
 *  <p>
 * The symbols() method has been overridden to handle the weirdness of a
 * location crossing the origin.
 * </p>
 *
 * @author Matthew Pocock
 * @author Mark Schreiber
 * @since 1.2
 */

public class CircularLocation

extends AbstractLocationDecorator {

  private final int length;
  private int fivePrimeEnd;
  private LinkedList fivePrimeSortedBlocks;

  private final boolean overlaps;



  public final int getLength() {

    return length;

  }


  /**
   * Does the Location overlap the origin (position 1) of the sequence?
   * <p> If it does the Location will internally be composed of a CompoundLocation.
   * @return true if it does, false otherwise
   */
  public boolean overlapsOrigin(){

    return overlaps;

  }


  /**
   * Constructs a CircularLocation by wrapping another Location
   * <strong>It is preferable to use LocationTools to make CircularLocations</strong>
   * @param wrapped the Location to wrap.
   * @param length the length of the Sequence
   */
  public CircularLocation(Location wrapped, int length) {
    this(wrapped, length, wrapped.getMin());
  }

  public CircularLocation(Location wrapped, int length, int fivePrimeEnd){
    super(wrapped);
    this.length = length;
    this.overlaps = CircularLocationTools.overlapsOrigin(this);

    //the 5' end must be the min of one of the blocks of the wrapped location
    fivePrimeSortedBlocks = new LinkedList();
    for(Iterator i = wrapped.blockIterator(); i.hasNext();){
      Location loc = (Location)i.next();
      fivePrimeSortedBlocks.add(loc);
      if(loc.getMin() == fivePrimeEnd){
        this.fivePrimeEnd = fivePrimeEnd;
      }
    }

    //reorder blocks so that block with 5' end is the first block
    Collections.sort(fivePrimeSortedBlocks, Location.naturalOrder);
    //check the first item to see if it is the five prime end
    while(((Location)fivePrimeSortedBlocks.getFirst()).getMin() != get5PrimeEnd()){
      //pop it off and send it to the back of the list
      Object o = fivePrimeSortedBlocks.removeFirst();
      fivePrimeSortedBlocks.addLast(o);
    }

    if(get5PrimeEnd() == 0){
      throw new IllegalArgumentException(
          "The 5' End must be either the minimum of the wrapped location or the minimum of one of its components");
    }
  }



  protected Location decorate(Location loc) {

    return new CircularLocation(loc, getLength());

  }



  public boolean contains(int p) {

    int pp = p % getLength() + (super.getMin() / getLength());



    return getWrapped().contains(pp);

  }





  public Location intersection(Location l) {

    return LocationTools.intersection(this,l);

  }

  public boolean overlaps(Location l) {

    return LocationTools.overlaps(this,l);

  }

  public Location union(Location l) {

    return LocationTools.union(this,l);

  }

  public boolean contains(Location l) {

    return LocationTools.contains(this,l);

  }

  public boolean equals(Object o){

    if((o instanceof Location)==false) return false;

    return LocationTools.areEqual(this, (Location)o);

  }


  public String toString(){
    //fixme to use 5' iterator

    StringBuffer sb = new StringBuffer(getWrapped().toString());

    sb.append("  (circular)");

    return sb.toString();

  }


  /**
   * Delegates to the wrapped location. Currently as locations that cross
   * the origin are wrapped CompoundLocations they are not considered contiguous.
   * This is desirable from the point of view of logical operations as it greatly
   * simplifies the calculations of things such as contains, overlaps etc.
   * @return true if the location is contiguous and doesn't overlap the origin
   */
  public boolean isContiguous() {
    return getWrapped().isContiguous();
  }

  /**
   * The point at which indicates the 5' end of the Location. This is needed as
   * compound circular locations have polarity. For example (18..30, 1..12) is
   * not the same as (1..12, 18..30). The 5' coordinate is derived during
   * construction of the Location. In particular during a union operation
   * the 5' coordinate is generally the 5' coordinate of the first location in
   * the union. In the case where this cannot be logically maintained the 5'
   * coordinate will revert to <code>getMin()</code>
   *
   * @return the most 5' coordinate
   * @see fivePrimeBlockIterator()
   * @see getMin()
   */
  public int get5PrimeEnd(){
    return fivePrimeEnd;
  }


  public SymbolList symbols(SymbolList seq) {
    SymbolList syms;
    Edit ed;

    //iterate over the Locations from the 5' end
    ListIterator i = this.fivePrimeBlockIterator();
    syms = ((Location)i.next()).symbols(seq);

    while(i.hasNext()){
      Location loc = (Location)i.next();
      SymbolList add = loc.symbols(seq);
      ed = new Edit(syms.length()+1,0,add);
      try {
        syms.edit(ed);
      }
      catch (Exception ex) {
        throw new BioError(ex,"Illegal edit operation");
      }
    }
    return syms;
  }

  /**
   * Iterates over the location blocks in order starting with the most 5'
   * @see blockIterator()
   * @see get5PrimeEnd()
   * @return a ListIterator
   */
  public ListIterator fivePrimeBlockIterator() {
    return fivePrimeSortedBlocks.listIterator();
  }

}

