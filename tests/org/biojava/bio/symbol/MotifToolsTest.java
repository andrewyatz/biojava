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

import java.util.Arrays;

import junit.framework.TestCase;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.utils.NestedError;

public class MotifToolsTest
    extends TestCase {
    
    protected String n;
    
    protected void setUp() {
      try {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        SymbolTokenization sTok = DNATools.getDNA().getTokenization("token");
        FiniteAlphabet na = (FiniteAlphabet) DNATools.n().getMatches();

        Symbol [] nSyms = (Symbol [])
            AlphabetManager.getAllSymbols(na).toArray(new Symbol [0]);
        char [] nChars = new char [nSyms.length];

        for (int i = 0; i < nSyms.length; i++)
        {
            nChars[i] = sTok.tokenizeSymbol(nSyms[i]).charAt(0);
        }

        Arrays.sort(nChars);
        sb.append(nChars);
        sb.append("]");
        n = sb.toString();
      } catch (Exception e) {
        throw new NestedError(e, "Couldn't initialize motif tools test");
      }
    }
      
    public MotifToolsTest(String name) {
        super(name);
    }

    public void testPlain() {
        doTest("atcg", "[-a][-t][-c][-g]");
    }

    public void testTwoStart() {
        doTest("aatcg", "[-a]{2}[-t][-c][-g]");
    }

    public void testThreeStart() {
        doTest("aaatcg", "[-a]{3}[-t][-c][-g]");
    }

    public void testTwoInternal() {
        doTest("attcg", "[-a][-t]{2}[-c][-g]");
    }

    public void testThreeInternal() {
        doTest("atttcg", "[-a][-t]{3}[-c][-g]");
    }

    public void testTwoEnd() {
        doTest("atcgg", "[-a][-t][-c][-g]{2}");
    }

    public void testThreeEnd() {
        doTest("atcggg", "[-a][-t][-c][-g]{3}");
    }

    public void testTwoOnly() {
        doTest("aa", "[-a]{2}");
    }

    public void testThreeOnly() {
        doTest("aaa", "[-a]{3}");
    }

    public void testAmbStart() {
        doTest("ngct", n + "[-g][-c][-t]");
    }

    public void testAmbMiddle() {
        doTest("anct", "[-a]" + n + "[-c][-t]");
    }

    public void testAmbEnd() {
        doTest("agcn", "[-a][-g][-c]" + n);
    }

    public void testTwoAmbOnly() {
        doTest("nn", n + "{2}");
    }

    void doTest(String pattern, String target) {
        try {
            assertEquals(target, MotifTools.createRegex(DNATools.createDNA(pattern)));
        } catch (IllegalSymbolException ise) {
            throw new org.biojava.utils.NestedError(ise);
        }
    }
}
