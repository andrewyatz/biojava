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

package org.biojava.bio.chromatogram;

import org.biojava.bio.chromatogram.Chromatogram;

import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.IntegerAlphabet;

/**
 * Utility class for dealing with {@link Chromatogram}s.
 *
 * @author Rhett Sutphin (<a href="http://genome.uiowa.edu/">UI CBCB</a>)
 */
public class ChromatogramTools {
    /** Static utility class */
    private ChromatogramTools() { }
    
    /**
     * Get the called DNA sequence from a chromatogram.  A synonym for
     * <code>chromat.getBaseCalls().symbolListForLabel(Chromatogram.DNA)</code>.
     */
    public static final SymbolList getDNASequence(Chromatogram chromat) {
        return chromat.getBaseCalls().symbolListForLabel(Chromatogram.DNA);
    }
    
    /**
     * Get the peak offsets for the called bases of a chromatogram.  A synonym 
     * for <code>chromat.getBaseCalls().symbolListForLabel(Chromatogram.OFFSETS)</code>.
     */
    public static final SymbolList getTraceOffsets(Chromatogram chromat) {
        return chromat.getBaseCalls().symbolListForLabel(Chromatogram.OFFSETS);
    }
    
    /**
     * Converts the peak offsets list of the given chromatogram
     * into a new <code>int</code> array.
     * <p>
     * The array is, of course, allocated and initialized at each call,
     * so using this method like this:
     * <pre>
     * for (int i = m ; i < n ; i++)
     *    doSomething(getTraceOffsetArray(c)[i]);
     * </pre>
     * is not recommended.
     */
    public static final int[] getTraceOffsetArray(Chromatogram chromat) {
        int[] array = new int[chromat.getSequenceLength()];
        for (int i = 0 ; i < array.length ; i++) {
            array[i] = getTraceOffset(chromat, i+1);
        }
        return array;
    }
    
    /**
     * Get a specific value from the trace offset sequence in the given
     * chromatogram and extract its <code>int</code> value.
     * 
     * @param chromat the chromatogram to examine
     * @param which which symbol in the trace offset sequence to
     *        get.  1-based index.
     */
    public static final int getTraceOffset(Chromatogram chromat, int which) {
        return getIntFromSymbolList(chromat.getBaseCalls().symbolListForLabel(Chromatogram.OFFSETS), which);
    }
    
    /**
     * Retrieves, unwraps, and returns an <code>int</code> from a
     * SymbolList containing {@link org.biojava.bio.symbol.IntegerAlphabet.IntegerSymbol}s.
     * @param list the target list
     * @param which which symbol to unwrap and return.  1-based index.
     */
    public static final int getIntFromSymbolList(SymbolList list, int which) {
        return ((IntegerAlphabet.IntegerSymbol) list.symbolAt(which)).intValue();
    }
    
}