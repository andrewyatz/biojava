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

package org.biojava.bio.program.homologene;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * a no-frills implementation of a Homologene Group
 *
 * @author David Huen
 */
public class SimpleOrthoPairSet
    extends AbstractOrthoPairSet
//    implements OrthoPairSet
{
    String name;
    Set orthologies = new HashSet();

    public class Iterator implements OrthoPairSet.Iterator
    {
        private java.util.Iterator setIterator;

        private Iterator(java.util.Iterator setIterator)
        {
            this.setIterator = setIterator;
        }

        public boolean hasNext()
        {
            return setIterator.hasNext();
        }

        public OrthoPair nextOrthoPair()
        {
            return (OrthoPair) setIterator.next();
        }

    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void addOrthoPair(OrthoPair orthology)
    {
        orthologies.add(orthology);
    }

    public void removeOrthoPair(OrthoPair orthology)
    {
        orthologies.remove(orthology);
    }

    public OrthoPairSet.Iterator iterator()
    {
        return new Iterator(orthologies.iterator());
    }

    public double getMinIdentity()
    {
        double min = 100.0;

        for (java.util.Iterator orthologiesI = orthologies.iterator();
             orthologiesI.hasNext(); )
        {
            OrthoPair currOrthoPair = (OrthoPair) orthologiesI.next();

            min = Math.min(min, currOrthoPair.getPercentIdentity());
        }

        return min;
    }

    public int size()
    {
        return orthologies.size();
    }

    public Set getTaxa()
    {
        Set taxa = new HashSet();

        for (java.util.Iterator orthoI = orthologies.iterator(); orthoI.hasNext(); ) {
            OrthoPair currOrtho = (OrthoPair) orthoI.next();

            // look up the Taxon
            taxa.add( currOrtho.getFirstOrthologue().getTaxon());
            taxa.add( currOrtho.getSecondOrthologue().getTaxon());
        }

        return taxa;
    }
}
