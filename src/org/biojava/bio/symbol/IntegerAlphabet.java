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
 *
 */


package org.biojava.bio.symbol;

import java.util.*;
import java.io.*;
import java.lang.ref.*;
import java.lang.reflect.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.io.*;
import org.biojava.utils.*;

/**
 ( <p>
 * An efficient implementation of an Alphabet over the infinite set of integer
 * values.
 * </p>
 *
 * <p>
 * This class can be used to represent lists of integer numbers as a
 * SymbolList with the alphabet IntegerAlphabet. These lists can then be
 * annotated with features, or fed into dynamic-programming algorithms, or
 * processed as per any other SymbolList object.
 * </p>
 *
 * <p>
 * Object identity should be used to decide if two IntegerSymbol objects are
 * the same. IntegerAlphabet ensures that all IntegerSymbol instances are
 * canonicalized.
 * </p>
 *
 * @author Matthew Pocock
 * @author Mark Schreiber
 */
public final class IntegerAlphabet
  extends
    Unchangeable
  implements
    Alphabet,
    Serializable
{
  /**
   * The singleton instance of the IntegerAlphabet class.
   */
  private static IntegerAlphabet INSTANCE;

  private Object writeReplace() throws ObjectStreamException {
    try {
      return new StaticMemberPlaceHolder(IntegerAlphabet.class.getField("INSTANCE"));
    } catch (NoSuchFieldException nsfe) {
      throw new NotSerializableException(nsfe.getMessage());
    }
  }

  /**
   * Construct a finite contiguous subset of the <code>IntegerAlphabet</code>.
   * Useful for making CrossProductAlphabets with other <code>FiniteAlphabet</code>s.
   *
   * @param min the lower bound of the Alphabet
   * @param max the upper bound of the Alphabet
   * @throws IllegalArgumentException if max < min
   * @return A FiniteAlphabet from min to max <b>inclusive</b>.
   */
  public static SubIntegerAlphabet getSubAlphabet(int min, int max)
  throws IllegalArgumentException {
    String name = "SubIntegerAlphabet["+min+".."+max+"]";
    try{
      return (SubIntegerAlphabet) (AlphabetManager.alphabetForName(name));
    }catch(Exception e){
      FiniteAlphabet a = new SubIntegerAlphabet(min, max);
      AlphabetManager.registerAlphabet(a.getName(),a);
    }

    return (SubIntegerAlphabet) (AlphabetManager.alphabetForName(name));
  }

  /**
   * Retrieve a SymbolList view of an array of integers.
   * <p>
   * The returned object is a view onto the underlying array, and does not copy
   * it. Changes made to the original array will alter the symulting SymbolList.
   *
   * @param iArray  the array of integers to view
   * @return a SymbolList over the IntegerAlphabet that represent the values in
   *         iArray
   */
  public static SymbolList fromArray(int [] iArray) {
    return new IntegerArray(iArray);
  }

  /**
   * Retrieve the single IntegerAlphabet instance.
   *
   * @return the singleton IntegerAlphabet instance
   */
  public static IntegerAlphabet getInstance() {
    if(INSTANCE == null) {
      INSTANCE = new IntegerAlphabet();
    }
    
    return INSTANCE;
  }

  /**
   * Canonicalization map for ints and references to symbols.
   */
    private Map intToSymRef;
    private Map symRefToInt;
    private ReferenceQueue queue;
  
  private IntegerAlphabet() {
      intToSymRef = new HashMap();
      symRefToInt = new HashMap();
      queue = new ReferenceQueue();
  }
  
  /**
   * Retrieve the Symbol for an int.
   *
   * @param val  the int to view
   * @return a IntegerSymbol embodying val
   */
  public synchronized IntegerSymbol getSymbol(int val) {
      Reference qref;
      while ((qref = queue.poll()) != null) {
	  // System.out.println("Clearing queue");
	  Integer qi = (Integer) symRefToInt.get(qref);
	  if (qi != null) {
	      intToSymRef.remove(qi);
	      symRefToInt.remove(qref);
	  }
	  qref.clear();
      }

      Integer i = new Integer(val);
      Reference ref = (Reference) intToSymRef.get(i);
      IntegerSymbol sym; // stop premature reference clearup
    
      if(ref == null || ref.get() == null) {
	  // how accessible do we want this? Soft or Weak?
	  sym = new IntegerSymbol(val);
	  ref = new WeakReference(sym, queue);
	  intToSymRef.put(i, ref);
	  symRefToInt.put(ref, i);
	  return sym;
      } else {
	  return (IntegerSymbol) ref.get();
      }
  }

  public Symbol getGapSymbol() {
    return AlphabetManager.getGapSymbol(getAlphabets());
  }

  public Annotation getAnnotation() {
    return Annotation.EMPTY_ANNOTATION;
  }

  public List getAlphabets() {
    return new SingletonList(this);
  }

  public Symbol getSymbol(List symList)
  throws IllegalSymbolException {
    throw new BioError("Unimplemneted method");
  }

  public Symbol getAmbiguity(Set symSet)
  throws IllegalSymbolException {
    throw new BioError("Unimplemneted method");
  }

  public boolean contains(Symbol s) {
    if(s instanceof IntegerSymbol) {
      return true;
    } else {
      return false;
    }
  }

  public void validate(Symbol s) throws IllegalSymbolException {
    if(!contains(s)) {
      throw new IllegalSymbolException(
        "Only symbols of type IntegerAlphabet.IntegerSymbol are valid for this alphabet.\n" +
        "(" + s.getClass() + ") " + s.getName()
      );
    }
  }

  public String getName() {
    return "Alphabet of all integers.";
  }

  /**
   * @param name Currently only "token" is supported.
   * @return an IntegerParser.
   * @author Mark Schreiber 3 May 2001.
   */
  public SymbolTokenization getTokenization(String name) {
    if(name.equals("token")){
      return new IntegerTokenization();
    }else{
      throw new NoSuchElementException(name + " parser not supported by IntegerAlphabet yet");
    }
  }

  /**
   * A single int value.
   * <p>
   * @author Matthew Pocock
   */
  public static class IntegerSymbol
    extends
      Unchangeable
    implements
      AtomicSymbol,
      Serializable
  {
    private final int val;
    private final Alphabet matches;

    public Annotation getAnnotation() {
      return Annotation.EMPTY_ANNOTATION;
    }

    public String getName() {
      return val + "";
    }

    public int intValue() {
      return val;
    }

    public Alphabet getMatches() {
      return matches;
    }

    public List getSymbols() {
      return new SingletonList(this);
    }

    public Set getBases() {
      return Collections.singleton(this);
    }

    protected IntegerSymbol(int val) {
      this.val = val;
      this.matches = new SingletonAlphabet(this);
    }

    public int hashCode(){
      int result = 17;
      result = 37*result+intValue();
      return result;
    }

    public boolean equals(Object o){
      if(o == this) return true;
      if(o instanceof IntegerSymbol){
        IntegerSymbol i = (IntegerSymbol) o;
        if (i.intValue() == this.intValue()) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   * A light-weight implementation of SymbolList that allows an array to
   * appear to be a SymbolList.
   *
   * @author Matthew Pocock
   */
  private static class IntegerArray
  extends AbstractSymbolList implements Serializable {
    private final int [] iArray;

    public Alphabet getAlphabet() {
      return INSTANCE;
    }

    public Symbol symbolAt(int i) {
      return new IntegerSymbol(iArray[i-1]);
    }

    public int length() {
      return iArray.length;
    }

    public IntegerArray(int [] iArray) {
      this.iArray = iArray;
    }
  }

  /**
   * A class to represent a finite contiguous subset of the infinite IntegerAlphabet
   *
   * @author Mark Schreiber
   * @author Matthew Pocock
   * @since 1.3
   */
  public static class SubIntegerAlphabet
  extends AbstractAlphabet {
    private int min;
    private int max;
    private String name; // cache this for performance

    /**
     * Construct a contiguous sub alphabet with the integers from min to max inclusive.
     */
    private SubIntegerAlphabet(int min, int max) throws IllegalArgumentException{
      if(max < min) {
        throw new IllegalArgumentException(
          "min must be less than max: " +
          min + " : " + max
        );
      }
      
      this.min = min;
      this.max = max;

      this.name = "SubIntegerAlphabet["+min+".."+max+"]";
    }
    
    public String getName() {
      return name;
    }

    protected boolean containsImpl(AtomicSymbol sym) {
      if(!IntegerAlphabet.getInstance().contains(sym)) {
        return false;
      }

      IntegerSymbol is = (IntegerSymbol) sym;
      return is.intValue() >= min && is.intValue() <= max;
    }

    /**
     * @param name Currently only "token" is supported.
     * @return an IntegerParser.
     */
    public SymbolTokenization getTokenization(String name) {
      if(name.equals("token")){
        return new IntegerTokenization();
      }else{
        throw new NoSuchElementException(name + " parser not supported by IntegerAlphabet yet");
      }
    }
    
    public Symbol getSymbol(int val)
    throws IllegalSymbolException {
      if(val < min || val > max) {
        throw new IllegalSymbolException(
          "Could not get Symbol for value " +
          val + " as it is not in the range " +
          min + " : " + max
        );
      }
      
      return IntegerAlphabet.getInstance().getSymbol(val);
    }
    
    public int size() {
      return max - min + 1;
    }
    
    public List getAlphabets() {
      return new SingletonList(this);
    }
    
    protected AtomicSymbol getSymbolImpl(List symL)
    throws IllegalSymbolException {
      return (AtomicSymbol) symL.get(0);
    }
    
    protected void addSymbolImpl(AtomicSymbol sym)
    throws ChangeVetoException {
      throw new ChangeVetoException(
        "Can't add symbols to immutable alphabet " +
        getName()
      );
    }
    
    public void removeSymbol(Symbol sym)
    throws ChangeVetoException {
      throw new ChangeVetoException(
        "Can't remove symbols from immutable alphabet " +
        getName()
      );
    }
    
    public SymbolList symbols()
    throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }
    
    public Iterator iterator() {
      return new Iterator() {
        int indx = min;
        
        public boolean hasNext() {
          return indx <= max;
        }
        
        public Object next() {
          try {
            Symbol sym = getSymbol(indx);
            indx++;
            return sym;
          } catch (IllegalSymbolException ise) {
            throw new BioError(
              ise,
              "Assertion Failure: symbol " + indx +
              " produced by iterator but not found in " + getName()
            );
          }
        }
        
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }
    
    public Annotation getAnnotation() {
      return Annotation.EMPTY_ANNOTATION;
    }
  }
}
