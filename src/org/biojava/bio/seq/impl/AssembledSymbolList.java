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

package org.biojava.bio.seq.impl;

import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.utils.*;
import org.biojava.bio.symbol.*;

/**
 * Support class for applications which need to patch together sections
 * of sequence into a single SymbolList.  This class isn't intended
 * for direct use in user code -- instead, it is a helper for people
 * implementing the full BioJava assembly model.  See SimpleAssembly
 * for an example.
 *
 * @author Thomas Down
 * @since 1.1
 */

public class AssembledSymbolList extends AbstractSymbolList {
  private boolean autoLength = true;
    private int length = 0;

    private SortedMap components;
    private List componentList;

    private final static Symbol N;
    private final static char nChar = 'n';

    static {
	try {
	    N = DNATools.getDNA().getTokenization("token").parseToken("" + nChar);
        } catch (BioException ex) {
            throw new BioError(ex);
        }
    }

    {
        components = new TreeMap(Location.naturalOrder);
        componentList = new ArrayList();
    }

    public void setLength(int len) {
    autoLength = false;
	length = len;
    }

    public void putComponent(ComponentFeature f) {
	components.put(f.getLocation(), f);
	componentList.clear();
	componentList.addAll(components.keySet());
    }

    public void putComponent(Location l, SymbolList sl) {
      if(sl == this) {
        throw new NestedError("Circular reference");
      }
	components.put(l, sl);
	componentList.clear();
	componentList.addAll(components.keySet());
    }

    public void removeComponent(Location loc) {
	components.remove(loc);
	componentList.clear();
	componentList.addAll(components.keySet());
    }

  public SymbolList getComponentSymbols(Location loc) {
    Object o = components.get(loc);
    if (o instanceof ComponentFeature) {
      ComponentFeature cf = (ComponentFeature) o;
      return cf.getSymbols();
    } else {
      return (SymbolList) o;
    }
  }

    public Set getComponentLocationSet() {
	return components.keySet();
    }

    /**
     * Find the location containing p in a sorted list of non-overlapping contiguous
     * locations.
     */

    private Location lastLocation = Location.empty;

  public Location locationOfPoint(int p) {
    if (lastLocation.contains(p)) {
      return lastLocation;
    }

    int first = 0;
    int last = componentList.size() - 1;

    while (first <= last) {
      int check = (first + last) / 2;
      Location checkL = (Location) componentList.get(check);
      if (checkL.contains(p)) {
        lastLocation = checkL;
        return checkL;
      }

      if (p < checkL.getMin()) {
        last = check - 1;
      } else {
        first = check + 1;
      }
    }

    return null;
  }

    public Location locationUpstreamOfPoint(int p) {
	int first = 0;
	int last = componentList.size() - 1;
	
	int check = 0;
	Location checkL = null;
	while (first <= last) {
	    check = (first + last) / 2;
	    checkL = (Location) componentList.get(check);
	    if (checkL.contains(p))
		return checkL;

	    if (p < checkL.getMin())
		last = check - 1;
	    else
		first = check + 1;
	}
	
	try {
	    if (p < checkL.getMin()) {
		return checkL;
	    } else {
		return (Location) componentList.get(check + 1);
	    }
	} catch (IndexOutOfBoundsException ex) {
	    return null;
	} catch (NullPointerException ex) {
	    return null;
	}
    }

    public Alphabet getAlphabet() {
	return DNATools.getDNA();
    }

    public int length() {
      if(autoLength) {
        Location last = (Location) componentList.get(componentList.size() - 1);
        return last.getMax();
      } else {
        return length;
      }
    }

  public Symbol symbolAt(int pos) {
      // System.out.println(this + "  symbolAt(" + pos + ")");
    Location l = locationOfPoint(pos);
    if (l != null) {
      SymbolList syms = getComponentSymbols(l);
      return syms.symbolAt(pos - l.getMin() + 1);
    }

    return N;
  }

  public SymbolList subList(int start, int end) {
      // System.out.println(this + "  subList(" + start + ", " + end + ")");
    Location l = locationOfPoint(start);
    if (l != null && l.contains(end)) {
      SymbolList symbols = getComponentSymbols(l);
      int tstart = start - l.getMin() + 1;
      int tend = end - l.getMin() + 1;
      return symbols.subList(tstart, tend);
    }

    // All is lost.  Fall back onto `view' subList from AbstractSymbolList

    return super.subList(start, end);
  }

    public String subStr(int start, int end) {
	if (start < 1 || end > length()) {
	    throw new IndexOutOfBoundsException("Range out of bounds: " + start + " - " + end);
	}

	StringBuffer sb = new StringBuffer();
	int pos = start;
	while (pos <= end) {
	    Location loc = locationOfPoint(pos);
	    if (loc != null) {
		SymbolList sl = getComponentSymbols(loc);
		int slStart = Math.max(1, pos - loc.getMin() + 1);
		int slEnd = Math.min(loc.getMax() - loc.getMin() + 1,
				     end - loc.getMin() + 1);
		sb.append(sl.subStr(slStart, slEnd));
		pos += (slEnd - slStart + 1);
	    } else {
		loc = locationUpstreamOfPoint(pos);
		int numNs;
		if (loc != null) {
		    numNs = Math.min(loc.getMin(), end + 1) - pos;
		    pos = loc.getMin();
		} else {
		    numNs = end - pos + 1;
		    pos = end + 1;
		}
		for (int i = 0; i < numNs; ++i) {
		    sb.append(nChar);
		}
	    }
	}
	
	return sb.toString();
    }
}
