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

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.io.*;

/**
 * An implementation of FiniteAlphabet that grows the alphabet to accomodate all
 * the characters seen while parsing a file.
 * <P>
 * The contains and validate methods will still work as for other alphabets, but
 * the parsers will generate new symbol objects for each token or name seen.
 * <P>
 * This is particularly useful when reading in arbitrary alphabet files where
 * you don't want to invest the time and effort writing a formal alphabet.
 *
 * @author Matthew Pocock
 */
public class AllTokensAlphabet
extends AbstractAlphabet
implements Serializable {
  private Map tokenToSymbol; // token->Symbol
  private Map nameToSymbol; // name->Symbol
  private Set symbols;
  private String name;
  private Annotation annotation;

  protected void addSymbolImpl(AtomicSymbol s) {
    symbols.add(s);
    Character token = new Character(s.getToken());
    if(!tokenToSymbol.keySet().contains(token)) {
      tokenToSymbol.put(token, s);
    }
    nameToSymbol.put(s.getName(), s);
  }

  public Iterator iterator() {
    return symbols.iterator();
  }
  
  public Annotation getAnnotation() {
    if(annotation == null)
      annotation = new SimpleAnnotation();
    return annotation;
  }
  
  protected boolean containsImpl(AtomicSymbol s) {
    return symbols.contains(s);
  }
  
  public String getName() {
    return name;
  }
  
  public List getAlphabets() {
    return new SingletonList(this);
  }
  
  public SymbolParser getParser(String name)
  throws NoSuchElementException {
    if(name.equals("name")) {
      return new NameParser(nameToSymbol) {
        public Symbol parseToken(String token) throws IllegalSymbolException {
          Symbol sym = (Symbol) nameToSymbol.get(token);
          if(sym == null) {
            sym = AlphabetManager.createSymbol(token.charAt(0), token, null);
            try {
              addSymbol(sym);
            } catch (ChangeVetoException cve) {
              throw new IllegalSymbolException(
                cve,
                "Couldn't parse '" + token + "'"
              );
            }
          }
          return sym;
        }
      };
    } else if(name.equals("token")) {
      return new SymbolParser() {
        public Alphabet getAlphabet() {
          return AllTokensAlphabet.this;
        }
        public SymbolList parse(String seq)
        throws IllegalSymbolException {
          List symList = new ArrayList(seq.length());
          for(int i = 0; i < seq.length(); i++) {
            symList.add(parseToken(seq.substring(i, i+1)));
          }
          try {
	    return new SimpleSymbolList(getAlphabet(), symList);
          } catch (IllegalSymbolException ex) {
	    throw new BioError(ex);
          }
        }

        public Symbol parseToken(String token)
        throws IllegalSymbolException {
          char c = token.charAt(0);
          Character ch = new Character(c);
          Symbol s = (Symbol) tokenToSymbol.get(ch);
          if(s == null) {
            s = AlphabetManager.createSymbol(c, token, null);
            try {
              addSymbol(s);
            } catch (ChangeVetoException cve) {
              throw new IllegalSymbolException(
                cve,
                "Can't add symbol '" + token + "'"
              );
            }
          }
          return s;
        }

	public StreamParser parseStream(SeqIOListener l) {
	    final SeqIOListener listener = l;

	    return new StreamParser() {
		public void characters(char[] data, int start, int len) 
		    throws IllegalSymbolException
		{
		    Symbol[] syms = new Symbol[len];
		    for (int i = 0; i < len; ++i) {
			syms[i] = parseToken("" + data[start + i]);
		    }
		    try {
			listener.addSymbols(AllTokensAlphabet.this, syms, 0, len);
		    } catch (IllegalAlphabetException ex) {
			throw new BioError(ex);
		    }
		}

		public void close() {
		}
	    } ;
	}
      };
    } else {
      throw new NoSuchElementException("No parser for " + name +
      " known in alphabet " + getName());
    }
  }
  
  public SymbolList symbols() {
      try {
	  return new SimpleSymbolList(this, new ArrayList(symbols));
      } catch (IllegalSymbolException ex) {
	  throw new BioError(ex);
      }
  }
  
  public int size() {
    return symbols.size();
  }
    
  public void removeSymbol(Symbol sym) throws IllegalSymbolException {
    throw new IllegalSymbolException(
      "Can't remove symbols from alphabet: " + sym.getName() +
      " in " + getName()
    );
  }
  
  protected AtomicSymbol getSymbolImpl(List symList)
  throws IllegalSymbolException {
    return (AtomicSymbol) symList.get(0);
  }
  
  public AllTokensAlphabet(String name) {
    this.name = name;
    this.symbols = new HashSet();
    this.tokenToSymbol = new HashMap();
    this.nameToSymbol = new HashMap();
  }
}
