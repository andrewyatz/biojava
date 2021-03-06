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
 * Created on 01-21-2010
 *
 * 
 * @auther Scooter Willis
 *
 */
package org.biojava3.core.sequence.transcription;

import org.biojava.bio.seq.RNATools;
import org.biojava.bio.symbol.SymbolList;
import org.biojava3.core.sequence.RNASequence;

public class DefaultRNAProteinTranscription implements RNAProteinTranscription {

    public String translate(RNASequence rnaCodingSequence) throws Exception {
        String codingSequence = rnaCodingSequence.getString();
        SymbolList rnaSymbolList = RNATools.createRNA(codingSequence);

        //truncate to a length divisible by three.
        rnaSymbolList = rnaSymbolList.subList(1, rnaSymbolList.length() - (rnaSymbolList.length() % 3));
        SymbolList aminoAcidSymbolList = RNATools.translate(rnaSymbolList);

        return aminoAcidSymbolList.seqString();
    }
}
