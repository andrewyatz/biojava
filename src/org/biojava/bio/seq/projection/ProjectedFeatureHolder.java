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

package org.biojava.bio.seq.projection;

import java.util.Iterator;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.projection.*;
import org.biojava.bio.seq.AbstractFeatureHolder;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.utils.*;

/**
 * Helper class for projecting Feature objects into an alternative
 * coordinate system.  This class offers a view onto a set of features,
 * projecting them into a different coordinate system, and also changing
 * their <code>parent</code> property.  The destination coordinate system
 * can run in the opposite direction from the source, in which case the
 * <code>strand</code> property of StrandedFeatures is flipped.
 *
 * <p>
 * The projected features returned by this class are small proxy objects.
 * Proxy classes are autogenerated on demand for any sub-interface of
 * <code>Feature</code> by the <code>ProjectionEngine</code> class.
 * </p>
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @since 1.1
 */

public final class ProjectedFeatureHolder
        extends AbstractFeatureHolder
        implements FeatureHolder {
    private final ProjectionContext context;
    private final ChangeListener underlyingFeaturesChange;
    private FeatureHolder topLevelFeatures;

  // don't ask why we need an initializer - blame it on the statics
  {
    underlyingFeaturesChange = new ChangeListener() {
        public void preChange(ChangeEvent e)
            throws ChangeVetoException
        {
            if (hasListeners()) {
                ChangeEvent cev2 = forwardChangeEvent(e);
                if (cev2 != null) {
                    getChangeSupport(FeatureHolder.FEATURES).firePreChangeEvent(cev2);
                }
            }
        }

        public void postChange(ChangeEvent e) {
           if (hasListeners()) {
               ChangeEvent cev2 = forwardChangeEvent(e);
                if (cev2 != null) {
                    getChangeSupport(FeatureHolder.FEATURES).firePostChangeEvent(cev2);
                }
           }
        }
    } ;
  }

  public ProjectedFeatureHolder(
          ProjectionContext context)
  {
    this.context = context;
    context.getUnprojectedFeatures().addChangeListener(underlyingFeaturesChange);
  }

  public ProjectionContext getContext() {
    return context;
  }

  private FeatureHolder getTopLevelFeatures() {
    if (topLevelFeatures == null) {
      topLevelFeatures = makeProjectionSet(context.getUnprojectedFeatures());
    }
    return topLevelFeatures;
  }

  //
  // Normal FeatureHolder methods get delegated to our top-level ProjectionSet
  //

  public Iterator features() {
    return getTopLevelFeatures().features();
  }

  public int countFeatures() {
    return getTopLevelFeatures().countFeatures();
  }

  public boolean containsFeature(Feature f) {
    return getTopLevelFeatures().containsFeature(f);
  }

  public FeatureHolder filter(FeatureFilter ff) {
    return getTopLevelFeatures().filter(ff);
  }

  public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
    return getTopLevelFeatures().filter(ff, recurse);
  }

  public Feature createFeature(Feature.Template templ)
    throws ChangeVetoException, BioException
  {
    return context.createFeature(templ);
  }

    public void removeFeature(Feature dyingChild)
        throws ChangeVetoException, BioException
	{
    context.removeFeature(dyingChild);
	}

    public FeatureFilter getSchema() {
        return getTopLevelFeatures().getSchema();
    }

    /**
     * Called internally to construct a lightweight projected view of a set of
     * features
     */

    protected FeatureHolder makeProjectionSet(FeatureHolder fh) {
        return context.projectFeatures(fh);
    }

  /**
   * Called internally to generate a forwarded version of a ChangeEvent from our
   * underlying FeatureHolder
   */

  protected ChangeEvent forwardChangeEvent(ChangeEvent cev) {
      return new ChangeEvent(this,
                             cev.getType(),
                             cev.getChange(),
                             cev.getPrevious(),
                             cev);
  }

}
