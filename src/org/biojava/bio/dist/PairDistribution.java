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


package org.biojava.bio.dist;

import java.util.*;
import java.lang.ref.*;
import java.io.Serializable;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * Class for pairing up two independant distributions.
 */


public class PairDistribution
extends AbstractChangeable
implements Serializable, Distribution {
  private static Map cache;
  private static ListWrapper gopher;
  
  static {
    cache = new HashMap();
    gopher = new ListWrapper();
  }
  
  protected static Distribution getNullModel(Distribution first, Distribution second) {
    synchronized(cache) {
      first = first.getNullModel();
      second = second.getNullModel();
      List distL = Arrays.asList(new Object [] { first, second } );
      gopher.setList(distL);
      SoftReference ref = (SoftReference) cache.get(gopher);
      Distribution dist;
      if(ref == null) {
        dist = new PairDistribution(first, second);
        cache.put(new ListWrapper(distL), new SoftReference(dist));
      } else {
        dist = (Distribution) ref.get();
        if(dist == null) {
          dist = new PairDistribution(first, second);
          cache.put(new ListWrapper(distL), new SoftReference(dist));
        }
      }
      return dist;
    }
  }  
  
  private Distribution first;
  private Distribution second;
  private Alphabet alphabet;
  
  private Distribution nullModel;
  
  public Alphabet getAlphabet() {
    return alphabet;
  }
  
  public Distribution getNullModel() {
    return getNullModel(first, second);
  }
  
  public void setNullModel(Distribution nullModel)
  throws IllegalAlphabetException, ChangeVetoException {
    throw new ChangeVetoException(
      "PairDistribution objects can't have their null models changed."
    );
  }
  
  /**
   * Register this paired distribution with a model trainer.
   * @param trainer the trainer to register this distribution with.
   */
  public void registerWithTrainer(org.biojava.bio.dp.ModelTrainer trainer) {
    trainer.registerDistribution(first);
    trainer.registerDistribution(second);
    
    trainer.registerTrainer(this, new PairTrainer());
  }

  public double getWeight(Symbol sym)
  throws IllegalSymbolException {
    if(sym instanceof BasisSymbol) {
      List symL = ((BasisSymbol) sym).getSymbols();
      Symbol f = (Symbol) symL.get(0);
      Symbol s = (Symbol) symL.get(1);
      
      return first.getWeight(f) * second.getWeight(s);
    } else {
      double score = 0.0;
      for(Iterator i = ((FiniteAlphabet) sym.getMatches()).iterator();
      i.hasNext(); ) {
        AtomicSymbol s = (AtomicSymbol) i.next();
        score += getWeight(s);
      }
      return score;
    }
  }
  
  public void setWeight(Symbol sym, double weight)
  throws ChangeVetoException {
    throw new ChangeVetoException(
      "Can't set the weight directly in a PairDistribution. " +
      "You must set the weights in the underlying distributions."
    );
  }
  
  public PairDistribution(Distribution first, Distribution second) {
    this.first = first;
    this.second = second;
    this.alphabet = AlphabetManager.getCrossProductAlphabet(
      Arrays.asList(new Alphabet[] {
        first.getAlphabet(), second.getAlphabet()
      })
    );
  }
  
  public void registerWithTrainer(DistributionTrainerContext dtc) {
    dtc.registerTrainer(this, new PairTrainer());
  }
  
  private class PairTrainer
  extends IgnoreCountsTrainer
  implements Serializable {
    public void addCount(
      DistributionTrainerContext dtc, Symbol sym, double times
    ) throws IllegalSymbolException {
      getAlphabet().validate(sym);
      if(!(sym instanceof AtomicSymbol)) {
        throw new IllegalSymbolException(
          "Can't add counts for ambiguity symbols. Got: " +
          sym.getName()
        );
      }
      // FIXME: should get matches for symbol &
      // divide count by null model ratioes.
      List symL = ((BasisSymbol) sym).getSymbols();
      Symbol f = (Symbol) symL.get(0);
      Symbol s = (Symbol) symL.get(1);
      
      dtc.addCount(first, f, times);
      dtc.addCount(second, s, times);
    }
  }
  
  public Symbol sampleSymbol() {
    try {
      return getAlphabet().getSymbol(Arrays.asList( new Symbol[] {
        first.sampleSymbol(),
        second.sampleSymbol()
      }));
    } catch (IllegalSymbolException ise) {
      throw new BioError(ise, "Couldn't sample symbol");
    }
  }
}
