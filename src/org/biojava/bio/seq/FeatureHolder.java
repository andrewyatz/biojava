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
 * The interface for objects that contain features.
 * <P>
 * Feature holders abstract the containment of a feature from the objects
 * that implements both the real container or the features. FeatureHolders are
 * like bags of features. I guess they should be like sets, but who cares.
 * <P>
 * The add/remove methods need shawing up to throw exceptions & allow read-only
 * implementations.
 *
 * @author Matthew Pocock
 */
public interface FeatureHolder {
  /**
   * Count how many features are contained.
   *
   * @return  a positive integer or zero, equal to the number of features
   *          contained
   */
  int countFeatures();
  
  /**
   * Iterate over the features in no well defined order.
   *
   * @return  an Iterator
   */
  Iterator features();
  
  /**
   * Add a feature to this holder.
   *
   * @param f the Feature to add
   */
  void addFeature(Feature f);
  
  /**
   * Remove a feature from this holder.
   *
   * @param f the feature to remove
   */
  void removeFeature(Feature f);

  /**
   * Return a new FeatureHolder that contains all of the children of this one
   * that passed the filter fc.
   *
   * @param fc  the FeatureFilter to apply
   * @param recurse true if all features-of-features should be scanned, and a
   *                single flat collection of features returned, or false if
   *                just emediate children should be filtered
   */
  FeatureHolder filter(FeatureFilter fc, boolean recurse);
}
