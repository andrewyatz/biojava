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

import java.io.*;

import org.biojava.bio.*;

/**
 * A no-frills implementation of a symbol.
 *
 * @author Matthew Pocock
 */
public class SimpleSymbol implements Symbol, Serializable {
  private Annotation annotation;
  private char token;
  private String name;
  private Alphabet matches;
  
  public Annotation getAnnotation() {
    if(annotation == null)
      annotation = new SimpleAnnotation();
    return this.annotation;
  }

  public char getToken() {
    return this.token;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public Alphabet getMatches() {
    return this.matches;
  }
  
  /**
   * Create a new SimpleSymbol.
   *
   * @param token  the char to represent this symbol when sequences are
   *                stringified
   * @param name  the long name
   * @param matches the Alphabet of symbols that this symbol can match
   * @param annotation the annotation
   */
  public SimpleSymbol(
    char token,
    String name,
    Alphabet matches,
    Annotation annotation
  ) {
    this.token = token;
    this.name = name;
    this.annotation = annotation;
    this.matches = matches;
  }

  public String toString() {
    return super.toString() + " " + token;
  }
}
