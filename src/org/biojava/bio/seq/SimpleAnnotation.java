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


package org.biojava.bio.seq;

import java.util.*;

/**
 * A no-frills implementation of Annotation that is just a wrapper around a Map.
 *
 * @author Matthew Pocock
 */
public class SimpleAnnotation implements Annotation {
  /**
   * The properties map.
   * <P>
   * This may be null if no property values have yet been set.
   */
  private Map properties;
  
  /**
   * Retrieves properties, potentialy creating it if it was null.
   *
   * @return the properties Map
   */
  protected final Map getProperties() {
    if(!propertiesAllocated())
      properties = new HashMap();
    return properties;
  }

  /**
   * A convenience method to see if we have allocated the properties map yet.
   *
   * @returns true if properties is set and false otherwise
   */
  protected final boolean propertiesAllocated() {
    return properties != null;
  }

  public Object getProperty(Object key) throws IllegalArgumentException {
    if(propertiesAllocated())
      return getProperties().get(key);
    throw new IllegalArgumentException("Property " + key + " unknown");
  }

  public void setProperty(Object name, Object value) {
    getProperties().put(name, value);
  }

  public Set keys() {
    return properties.keySet();
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer("{");
    Map prop = getProperties();
    Iterator i = prop.keySet().iterator();
    if(i.hasNext()) {
      Object key = i.next();
      sb.append(key + "=" + prop.get(key));
    }
    while(i.hasNext()) {
      Object key = i.next();
      sb.append("," + key + "=" + prop.get(key));
    }
    sb.append("}");
    return sb.toString();
  }
}

