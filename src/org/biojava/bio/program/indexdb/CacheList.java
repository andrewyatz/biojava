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

package org.biojava.bio.program.indexdb;

import java.util.*;
import org.biojava.utils.*;

/**
 * <code>CacheList</code> is a decorator for
 * <code>SearchableList</code>s which maintains an in-memory cache of
 * list elements.
 *
 * @author Unknown
 * @author Keith James
 */
class CacheList
    extends
        AbstractList
    implements
        SearchableList
{
    private SearchableList delegate;
    private List shadow;

    /**
     * Creates a new <code>CacheList</code> instance.
     *
     * @param delegate a <code>SearchableList</code>.
     */
    public CacheList(SearchableList delegate) {
        this.delegate = delegate;
        shadow = new ArrayList();
        int l = delegate.size();
        for(int i = 0; i < l; i++) {
            shadow.add(null);
        }
    }

    public int size() {
        return shadow.size();
    }

    public Object get(int indx) {
        Object o = shadow.get(indx);
        if(o == null) {
            o = delegate.get(indx);
        }

        return o;
    }

    public Object set(int indx, Object val) {
        return shadow.set(indx, val);
    }

    public boolean add(Object val) {
        return shadow.add(val);
    }

    public Object search(String id) {
        return ((SearchableList) delegate).search(id);
    }

    public List searchAll(String id) {
        return ((SearchableList) delegate).searchAll(id);
    }

    public void commit()
        throws NestedException {
        delegate.clear();

        for(Iterator i = shadow.iterator(); i.hasNext(); ) {
            delegate.add(i.next());
        }

        ((Commitable) delegate).commit();
    }

    public void rollback() {
        ((Commitable) delegate).rollback();
        shadow.clear();
        int l = delegate.size();
        for(int i = 0; i < l; i++) {
            shadow.add(null);
        }
    }

    public Comparator getComparator() {
        return delegate.getComparator();
    }
}
