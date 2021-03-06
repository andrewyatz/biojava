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
 * Created on June 14, 2010
 * Author: Mark Chapman
 */

package org.biojava3.alignment;

import java.util.List;

import org.biojava3.alignment.template.AlignedSequence;
import org.biojava3.alignment.template.AlignedSequence.Step;
import org.biojava3.alignment.template.Profile;
import org.biojava3.alignment.template.SequencePair;
import org.biojava3.core.sequence.template.Compound;
import org.biojava3.core.sequence.template.Sequence;

/**
 * Implements a data structure for the results of pairwise sequence alignment.
 *
 * @author Mark Chapman
 * @param <S> each element of the alignment {@link Profile} is of type S
 * @param <C> each element of an {@link AlignedSequence} is a {@link Compound} of type C
 */
public class SimpleSequencePair<S extends Sequence<C>, C extends Compound> extends SimpleProfile<S, C>
        implements SequencePair<S, C> {

    private int identicals = -1, similars = -1;

    /**
     * Creates a pair profile for the given sequences.
     *
     * @param query the first sequence of the pair
     * @param target the second sequence of the pair
     * @param sx lists whether the query sequence aligns a {@link Compound} or gap at each index of the alignment
     * @param sy lists whether the target sequence aligns a {@link Compound} or gap at each index of the alignment
     * @throws IllegalArgumentException if alignments differ in size or given sequences do not fit in alignments
     */
    public SimpleSequencePair(S query, S target, List<Step> sx, List<Step> sy) {
        super(query, target, sx, sy);
    }

    @Override
    public C getCompoundInQueryAt(int alignmentIndex) {
        return getAlignedSequence(1).getCompoundAt(alignmentIndex);
    }

    @Override
    public C getCompoundInTargetAt(int alignmentIndex) {
        return getAlignedSequence(2).getCompoundAt(alignmentIndex);
    }

    @Override
    public int getIndexInQueryAt(int alignmentIndex) {
        return getAlignedSequence(1).getSequenceIndexAt(alignmentIndex);
    }

    @Override
    public int getIndexInQueryForTargetAt(int targetIndex) {
        return getAlignedSequence(1).getSequenceIndexAt(getAlignedSequence(2).getAlignmentIndexAt(targetIndex));
    }

    @Override
    public int getIndexInTargetAt(int alignmentIndex) {
        return getAlignedSequence(2).getSequenceIndexAt(alignmentIndex);
    }

    @Override
    public int getIndexInTargetForQueryAt(int queryIndex) {
        return getAlignedSequence(2).getSequenceIndexAt(getAlignedSequence(1).getAlignmentIndexAt(queryIndex));
    }

    @Override
    public int getNumIdenticals() {
        if (identicals == -1) {
            identicals = 0;
            for (int i = 1; i <= getLength(); i++) {
                if (getCompoundInQueryAt(i).equalsIgnoreCase(getCompoundInTargetAt(i))) {
                    identicals++;
                }
            }
        }
        return identicals;
    }

    @Override
    public int getNumSimilars() {
        if (similars == -1) {
            similars = 0;
            for (int i = 1; i <= getLength(); i++) {
                if (getCompoundSet().compoundsEquivalent(getCompoundInQueryAt(i), getCompoundInTargetAt(i))) {
                    similars++;
                }
            }
        }
        return similars;
    }

    @Override
    public AlignedSequence<C> getQuery() {
        return getAlignedSequence(1);
    }

    @Override
    public AlignedSequence<C> getTarget() {
        return getAlignedSequence(2);
    }

}
