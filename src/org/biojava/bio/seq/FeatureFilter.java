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

import java.io.Serializable;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.homol.SimilarityPairFeature;

/**
 * A filter for accepting or rejecting a feature.
 * <p>
 * This may implement arbitrary rules, or be based upon the feature's
 * annotation, type, location or source.
 * <p>
 * If the filter is to be used in a remote process, it is recognized that it may
 * be serialized and sent over to run remotely, rather than each feature being
 * retrieved locally.
 *
 * @since 1.0
 * @author Matthew Pocock
 * @author Thomas Down
 */

public interface FeatureFilter extends Serializable {
  /**
   * This method determines whether a feature is to be accepted.
   *
   * @param f the Feature to evaluate
   * @return  true if this feature is to be selected in, or false if it is to be ignored
   */
  boolean accept(Feature f);

  /**
   * All features are selected in with this filter.
   */
  static final public FeatureFilter all = new AcceptAllFilter() {};

  /**
   * No features are selected in with this filter.
   */
  static final public FeatureFilter none = new AcceptNoneFilter() {};


  /**
   *  A filter that returns all features not accepted by a child filter.
   *
   * @author Thomas Down
   * @author Matthew Pocock
   * @since 1.0
   */
  public final static class Not implements FeatureFilter {
    FeatureFilter child;

    public FeatureFilter getChild() {
      return child;
    }

    public Not(FeatureFilter child) {
        this.child = child;
    }

    public boolean accept(Feature f) {
        return !(child.accept(f));
    }

    public boolean equals(Object o) {
      return
        (o instanceof Not) &&
        (((Not) o).getChild().equals(this.getChild()));
    }

    public int hashCode() {
      return getChild().hashCode();
    }

    public String toString() {
      return "Not(" + child + ")";
    }
  }

  /**
   *  A filter that returns all features accepted by both child filter.
   *
   * @author Thomas Down
   * @author Matthew Pocock
   * @since 1.0
   */
  public final static class And implements FeatureFilter {
    FeatureFilter c1, c2;

    public FeatureFilter getChild1() {
      return c1;
    }

    public FeatureFilter getChild2() {
      return c2;
    }

    public And(FeatureFilter c1, FeatureFilter c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public boolean accept(Feature f) {
        return (c1.accept(f) && c2.accept(f));
    }

    public boolean equals(Object o) {
      if(o instanceof FeatureFilter) {
        return FilterUtils.areEqual(this, (FeatureFilter) o);
      } else {
        return false;
      }
    }

    public int hashCode() {
      return getChild1().hashCode() ^ getChild2().hashCode();
    }

      public String toString() {
	  return "And(" + c1 + " , " + c2 + ")";
       }
  }

  /**
   *  A filter that returns all features accepted by at least one child filter.
   *
   * @author Thomas Down
   * @author Matthew Pocock
   * @since 1.0
   */
  public final static class Or implements FeatureFilter {
    FeatureFilter c1, c2;

    public FeatureFilter getChild1() {
      return c1;
    }

    public FeatureFilter getChild2() {
      return c2;
    }

    public Or(FeatureFilter c1, FeatureFilter c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public boolean accept(Feature f) {
        return (c1.accept(f) || c2.accept(f));
    }

    public boolean equals(Object o) {
      if(o instanceof FeatureFilter) {
        return FilterUtils.areEqual(this, (FeatureFilter) o);
      } else {
        return false;
      }
    }

    public int hashCode() {
      return getChild1().hashCode() ^ getChild2().hashCode();
    }

    public String toString() {
      return "Or(" + c1 + " , " + c2 + ")";
    }
  }

  /**
   * Construct one of these to filter features by type.
   *
   * @author Matthew Pocock
   * @since 1.0
   */
  final public static class ByType implements OptimizableFilter {
    private String type;

    public String getType() {
      return type;
    }

    /**
     * Create a ByType filter that filters in all features with type fields
     * equal to type.
     *
     * @param type  the String to match type fields against
     */
    public ByType(String type) {
        if (type == null) {
            throw new NullPointerException("Type may not be null");
        }
        this.type = type;
    }

    /**
     * Returns true if the feature has a matching type property.
     */
    public boolean accept(Feature f) {
      return type.equals(f.getType());
    }

    public boolean equals(Object o) {
      return
        (o instanceof ByType) &&
        (((ByType) o).getType().equals(this.getType()));
    }

    public int hashCode() {
      return getType().hashCode();
    }

    public boolean isProperSubset(FeatureFilter sup) {
      return this.equals(sup) || (sup instanceof AcceptAllFilter);
    }

    public boolean isDisjoint(FeatureFilter filt) {
      return (filt instanceof AcceptNoneFilter) || (
        (filt instanceof ByType) &&
        !getType().equals(((ByType) filt).getType())
      );
    }

    public String toString() {
      return "ByType(" + type + ")";
    }
  }

  /**
   * Construct one of these to filter features by source.
   *
   * @author Matthew Pocock
   * @since 1.0
   */
  public final static class BySource implements OptimizableFilter {
    private String source;

    public String getSource() {
      return source;
    }

    /**
     * Create a BySource filter that filters in all features which have sources
     * equal to source.
     *
     * @param source  the String to match source fields against
     */
    public BySource(String source) {
        if (source == null) {
            throw new NullPointerException("Source may not be null");
        }
        this.source = source;
    }

    public boolean accept(Feature f) { return source.equals(f.getSource()); }

    public boolean equals(Object o) {
      return
        (o instanceof BySource) &&
        (((BySource) o).getSource().equals(this.getSource()));
    }

    public boolean isProperSubset(FeatureFilter sup) {
      return this.equals(sup) || (sup instanceof AcceptAllFilter);
    }

    public int hashCode() {
      return getSource().hashCode();
    }

    public boolean isDisjoint(FeatureFilter filt) {
      return (filt instanceof AcceptNoneFilter) || (
        (filt instanceof BySource) &&
        !getSource().equals(((BySource) filt).getSource())
      );
    }

      public String toString() {
	  return "BySource(" + source + ")";
       }
  }

  /**
   * Filter which accepts only those filters which are an instance
   * of a specific Java class
   *
   * @author Thomas Down
   * @author Matthew Pocock
   * @since 1.1
   */

  public final static class ByClass implements OptimizableFilter {
    private Class clazz;

    public ByClass(Class clazz) {
        if (clazz == null) {
            throw new NullPointerException("Clazz may not be null");
        }
        if(!Feature.class.isAssignableFrom(clazz)) {
            throw new ClassCastException(
              "Filters by class must be over Feature classes: " +
              clazz
            );
        }
        this.clazz = clazz;
    }

    public boolean accept(Feature f) {
      return clazz.isInstance(f);
    }

    public Class getTestClass() {
      return clazz;
    }

    public boolean equals(Object o) {
      return
        (o instanceof ByClass) &&
        (((ByClass) o).getTestClass() == this.getTestClass());
    }

    public int hashCode() {
      return getTestClass().hashCode();
    }

    public boolean isProperSubset(FeatureFilter sup) {
      if(sup instanceof ByClass) {
        Class supC = ((ByClass) sup).getTestClass();
        return supC.isAssignableFrom(this.getTestClass());
      }
      return (sup instanceof AcceptAllFilter);
    }

    public boolean isDisjoint(FeatureFilter feat) {
      if(feat instanceof ByClass) {
        Class featC = ((ByClass) feat).getTestClass();
        return
          ! (featC.isAssignableFrom(getTestClass())) &&
          ! (getTestClass().isAssignableFrom(featC));
      } else if (feat instanceof ByComponentName) {
	  return !getTestClass().isAssignableFrom(ComponentFeature.class);
      }

      return (feat instanceof AcceptNoneFilter);
    }

    public String toString() {
      return "ByClass(" + clazz.getName() + ")";
    }
  }


  /**
   * Accept features with a given strandedness.
   *
   * @author Matthew Pocock
   * @since 1.1
   */
  public final static class StrandFilter implements OptimizableFilter {
    private StrandedFeature.Strand strand;

    /**
     * Build a new filter that matches all features of a given strand.
     *
     * @param strand the Strand to match
     */
    public StrandFilter(StrandedFeature.Strand strand) {
      this.strand = strand;
    }

    /**
     * Retrieve the strand this matches.
     *
     * @return the Strand matched
     */
    public StrandedFeature.Strand getStrand() {
      return strand;
    }

    /**
     * Accept the Feature if it is an instance of StrandedFeature and matches
     * the value of getStrand().
     *
     * @param f the Feature to check
     * @return true if the strand matches, or false otherwise
     */
    public boolean accept(Feature f) {
      if(f instanceof StrandedFeature) {
        StrandedFeature sf = (StrandedFeature) f;
        return sf.getStrand() == strand;
      } else {
        return strand == StrandedFeature.UNKNOWN;
      }
    }

    public boolean equals(Object o) {
      return
        (o instanceof StrandFilter) &&
        (((StrandFilter) o).getStrand() == this.getStrand());
    }
    
    public int hashCode() {
      return getStrand().hashCode();
    }

    public boolean isProperSubset(FeatureFilter sup) {
      return this.equals(sup);
    }

    public boolean isDisjoint(FeatureFilter filt) {
      return (filt instanceof AcceptNoneFilter) || (
        (filt instanceof StrandFilter) &&
        ((StrandFilter) filt).getStrand() == getStrand()
      );
    }
  }
  
  /**
   * Accept features that reside on a sequence with a particular name.
   *
   * @author Matthew Pocock
   * @since 1.3
   */
  public final static class BySequeneName
  implements OptimizableFilter {
    private String seqName;
    
    public BySequeneName(String seqName) {
      this.seqName = seqName;
    }
    
    public String getSequenceName() {
      return seqName;
    }
    
    public boolean accept(Feature f) {
      return f.getSequence().getName().equals(seqName);
    }
    
    public boolean isProperSubset(FeatureFilter sup) {
      return equals(sup);
    }
    
    public boolean isDisjoint(FeatureFilter filt) {
      return !equals(filt);
    }

    public boolean equals(Object o) {
      return
       (o instanceof BySequeneName) &&
       ((BySequeneName) o).getSequenceName().equals(seqName);
    }
    
    public int hashCode() {
      return seqName.hashCode();
    }
  }
  
  /**
   *  A filter that returns all features contained within a location.
   *
   * @author Matthew Pocock
   * @since 1.0
   */
  public final static class ContainedByLocation implements OptimizableFilter {
    private Location loc;

    public Location getLocation() {
      return loc;
    }

    /**
     * Creates a filter that returns everything contained within loc.
     *
     * @param loc  the location that will contain the accepted features
     */
    public ContainedByLocation(Location loc) {
        if (loc == null) {
            throw new NullPointerException("Loc may not be null");
        }
        this.loc = loc;
    }

    /**
     * Returns true if the feature is within this filter's location.
     */
    public boolean accept(Feature f) {
      return loc.contains(f.getLocation());
    }

    public boolean equals(Object o) {
      return
        (o instanceof ContainedByLocation) &&
        (((ContainedByLocation) o).getLocation().equals(this.getLocation()));
    }

    public int hashCode() {
      return getLocation().hashCode();
    }

    public boolean isProperSubset(FeatureFilter sup) {
      if(sup instanceof ContainedByLocation) {
        Location supL = ((ContainedByLocation) sup).getLocation();
        return supL.contains(this.getLocation());
      } else if(sup instanceof OverlapsLocation) {
        Location supL = ((OverlapsLocation) sup).getLocation();
        return supL.contains(this.getLocation());
      }
      return (sup instanceof AcceptAllFilter);
    }

    public boolean isDisjoint(FeatureFilter filt) {
      if(filt instanceof ContainedByLocation) {
        Location loc = ((ContainedByLocation) filt).getLocation();
        return !getLocation().overlaps(loc);
      } else if (filt instanceof OverlapsLocation) {
	  Location filtL = ((OverlapsLocation) filt).getLocation();
	  return !filtL.overlaps(this.getLocation());
      }

      return (filt instanceof AcceptNoneFilter);
    }

    public String toString() {
      return "ContainedBy(" + loc + ")";
    }
  }

  /**
   *  A filter that returns all features overlapping a location.
   *
   * @author Matthew Pocock
   * @since 1.0
   */
  public final static class OverlapsLocation implements OptimizableFilter {
    private Location loc;

    public Location getLocation() {
      return loc;
    }

    /**
     * Creates a filter that returns everything overlapping loc.
     *
     * @param loc  the location that will overlap the accepted features
     */
    public OverlapsLocation(Location loc) {
        if (loc == null) {
            throw new NullPointerException("Loc may not be null");
        }
        this.loc = loc;
    }

    /**
     * Returns true if the feature overlaps this filter's location.
     */
    public boolean accept(Feature f) {
      return loc.overlaps(f.getLocation());
    }

    public boolean equals(Object o) {
      return
        (o instanceof OverlapsLocation) &&
        (((OverlapsLocation) o).getLocation().equals(this.getLocation()));
    }

    public int hashCode() {
      return getLocation().hashCode();
    }

    public boolean isProperSubset(FeatureFilter sup) {
      if(sup instanceof OverlapsLocation) {
        Location supL = ((OverlapsLocation) sup).getLocation();
        return supL.contains(this.getLocation());
      }
      return (sup instanceof AcceptAllFilter);
    }

    public boolean isDisjoint(FeatureFilter filt) {
      if (filt instanceof ContainedByLocation)  {
        Location loc = ((ContainedByLocation) filt).getLocation();
        return !getLocation().overlaps(loc);
      }
      return (filt instanceof AcceptNoneFilter);
    }
    
    public String toString() {
      return "Overlaps(" + loc + ")";
    }
  }
  
  /**
   * A filter that returns all features that have an annotation bundle that is of a given
   * annotation type.
   *
   * @author Matthew Pocock
   * @since 1.3
   */
  public static class ByAnnotationType
  implements OptimizableFilter {
    private AnnotationType type;
    
    protected ByAnnotationType() {
      this(AnnotationType.ANY);
    }
    
    public ByAnnotationType(AnnotationType type) {
      this.type = type;
    }
    
    public AnnotationType getType() {
      return type;
    }
    
    protected void setType(AnnotationType type) {
      this.type = type;
    }
    
    public boolean accept(Feature f) {
      return type.instanceOf(f.getAnnotation());
    }
    
    public boolean equals(Object o) {
      if(o instanceof ByAnnotationType) {
        ByAnnotationType that = (ByAnnotationType) o;
        return this.getType() == that.getType();
      }
      
      return false;
    }
    
    public int hashCode() {
      return getType().hashCode();
    }
    
    public boolean isDisjoint(FeatureFilter filter) {
      if(filter instanceof AcceptNoneFilter) {
        return true;
      } else if(filter instanceof ByAnnotationType) {
        // check for common property names
        ByAnnotationType that = (ByAnnotationType) filter;
        Set props = that.getType().getProperties();
        Set ourProps = new HashSet(getType().getProperties());
        ourProps.retainAll(props);
        if(ourProps.isEmpty()) {
          return false; // we can't prove they are disjoint because there is nothing to check for dissagreements:
        }
        for(Iterator i = ourProps.iterator(); i.hasNext(); ) {
          Object prop = i.next();
          Location thisC = this.getType().getCardinalityConstraint(prop);
          Location thatC = that.getType().getCardinalityConstraint(prop);
          
          if(LocationTools.intersection(thisC, thatC) == Location.empty) {
            return true;
          }
          
          PropertyConstraint thisP = this.getType().getPropertyConstraint(prop);
          PropertyConstraint thatP = that.getType().getPropertyConstraint(prop);
          return
            !thisP.subConstraintOf(thatP) &&
            !thatP.subConstraintOf(thisP);
        }
      }
      
      return false;
    }
    
    public boolean isProperSubset(FeatureFilter filter) {
      if(filter instanceof ByAnnotationType) {
        ByAnnotationType that = (ByAnnotationType) filter;

        Set thisProps = this.getType().getProperties();
        Set thatProps = that.getType().getProperties();
        for(Iterator i = that.getType().getProperties().iterator(); i.hasNext(); ) {
          Object prop = i.next();
          
          if(!thisProps.contains(prop)) {
            return false;
          }
          
          Location thisC = this.getType().getCardinalityConstraint(prop);
          Location thatC = that.getType().getCardinalityConstraint(prop);
          PropertyConstraint thisP = this.getType().getPropertyConstraint(prop);
          PropertyConstraint thatP = that.getType().getPropertyConstraint(prop);
          
          if(
            !thatP.subConstraintOf(thisP) ||
            !LocationTools.contains(thatC, thisC)
          ) {
            return false;
          }
        }
        
        return true;
      }
      
      return false;
    }
    
    public String toString() {
      return "ByAnnotationType {" + type + "}";
    }
  }

  /**
   * Retrieve features that contain a given annotation with a given value.
   *
   * @author Matthew Pocock
   * @author Keith James
   * @since 1.1
   */
  public final static class ByAnnotation
  extends ByAnnotationType {
    private Object key;
    private Object value;

    /**
     * Make a new ByAnnotation that will accept features with an annotation
     * bundle containing 'value' associated with 'key'.
     *
     * @param key  the Object used as a key in the annotation
     * @param value the Object associated with key in the annotation
     */
    public ByAnnotation(Object key, Object value) {
      this.key = key;
      this.value = value;
      
      AnnotationType.Impl type = new AnnotationType.Impl();
      type.setConstraints(
        key,
        new PropertyConstraint.ExactValue(value),
        CardinalityConstraint.ONE
      );
      setType(type);
    }

    public Object getKey() {
      return key;
    }

    public Object getValue() {
      return value;
    }
  }

  /**
   * Retrieve features that contain a given annotation with any value.
   *
   * @author Matthew Pocock
   * @author Keith James
   * @since 1.1
   */
  public final static class HasAnnotation
  extends ByAnnotationType {
    private Object key;

    /**
     * Make a new ByAnnotation that will accept features with an annotation
     * bundle containing any value associated with 'key'.
     *
     * @param key  the Object used as a key in the annotation
     */
    public HasAnnotation(Object key) {
      this.key = key;
      
      AnnotationType.Impl type = new AnnotationType.Impl();
      type.setConstraints(
        key,
        PropertyConstraint.ANY,
        CardinalityConstraint.ONE_OR_MORE
      );
      setType(type);
    }

    public Object getKey() {
      return key;
    }
  }

    /**
     * Filter by applying a nested <code>FeatureFilter</code> to the
     * parent feature.  Always <code>false</code> if the parent
     * is not a feature (e.g. top-level features, where the
     * parent is a sequence).
     *
     * @author Thomas Down
     * @since 1.2
     */

    public static class ByParent implements OptimizableFilter, Up {
        private FeatureFilter filter;

        public ByParent(FeatureFilter ff) {
            filter = ff;
        }

        public FeatureFilter getFilter() {
            return filter;
        }

        public boolean accept(Feature f) {
            FeatureHolder fh = f.getParent();
            if (fh instanceof Feature) {
                return filter.accept((Feature) fh);
            }

            return false;
        }

        public int hashCode() {
            return filter.hashCode() + 173;
        }

        public boolean equals(Object o) {
            if (! (o instanceof FeatureFilter.ByParent)) {
                return false;
            }

            FeatureFilter.ByParent ffbp = (FeatureFilter.ByParent) o;
            return ffbp.getFilter().equals(filter);
        }

        public boolean isProperSubset(FeatureFilter ff) {
            FeatureFilter ancFilter = null;
            if (ff instanceof FeatureFilter.ByParent) {
                ancFilter = ((FeatureFilter.ByParent) ff).getFilter();
            } else if (ff instanceof FeatureFilter.ByAncestor) {
                ancFilter = ((FeatureFilter.ByAncestor) ff).getFilter();
            }

            if (ancFilter != null) {
                return FilterUtils.areProperSubset(ancFilter, filter);
            } else {
                return false;
            }
        } 

        public boolean isDisjoint(FeatureFilter ff) {
            if (ff instanceof IsTopLevel) {
                return true;
            }
            
            FeatureFilter ancFilter = null;
            if (ff instanceof FeatureFilter.ByParent) {
                ancFilter = ((FeatureFilter.ByParent) ff).getFilter();
            }

            if (ancFilter != null) {
                return FilterUtils.areDisjoint(ancFilter, filter);
            } else {
                return false;
            }
        }
    }

    /**
     * Filter by applying a nested <code>FeatureFilter</code> to all
     * ancestor features.  Returns <code>true</code> if at least one
     * of them matches the filter.  Always <code>false</code> if the
     * parent is not a feature (e.g. top-level features, where the
     * parent is a sequence).
     *
     * @author Thomas Down
     * @since 1.2
     */

    public static class ByAncestor implements OptimizableFilter, Up {
        private FeatureFilter filter;

        public ByAncestor(FeatureFilter ff) {
            filter = ff;
        }

        public FeatureFilter getFilter() {
            return filter;
        }

        public boolean accept(Feature f) {
            do {
                FeatureHolder fh = f.getParent();
                if (fh instanceof Feature) {
                    f = (Feature) fh;
                    if (filter.accept(f)) {
                        return true;
                    }
                } else {
                    return false;
                }
            } while (true);
        }

        public int hashCode() {
            return filter.hashCode() + 186;
        }

        public boolean equals(Object o) {
            if (! (o instanceof FeatureFilter.ByAncestor)) {
                return false;
            }

            FeatureFilter.ByAncestor ffba = (FeatureFilter.ByAncestor) o;
            return ffba.getFilter().equals(filter);
        }

        public boolean isProperSubset(FeatureFilter ff) {
            FeatureFilter ancFilter = null;
            if (ff instanceof FeatureFilter.ByAncestor) {
                ancFilter = ((FeatureFilter.ByAncestor) ff).getFilter();
            }

            if (ancFilter != null) {
                return FilterUtils.areProperSubset(ancFilter, filter);
            } else {
                return false;
            }
        }

        public boolean isDisjoint(FeatureFilter ff) {
            if (ff instanceof IsTopLevel) {
                return true;
            }
            
            FeatureFilter ancFilter = null;
            if (ff instanceof FeatureFilter.ByParent) {
                ancFilter = ((FeatureFilter.ByParent) ff).getFilter();
            } else if (ff instanceof FeatureFilter.ByParent) {
                ancFilter = ((FeatureFilter.ByParent) ff).getFilter();
            }

            if (ancFilter != null) {
                return FilterUtils.areDisjoint(ancFilter, filter);
            } else {
                return false;
            }
        }
    }

    /**
     * Filter by applying a nested <code>FeatureFilter</code> to the
     * child features.  Always <code>false</code> if there are no children.
     *
     * @author Matthew Pocock
     * @author Thomas Down
     * @since 1.3
     */

    public static class ByChild implements OptimizableFilter, Down {
        private FeatureFilter filter;

        public ByChild(FeatureFilter ff) {
            filter = ff;
        }

        public FeatureFilter getFilter() {
            return filter;
        }

        public boolean accept(Feature f) {
          for(Iterator i = f.features(); i.hasNext(); ) {
            if(filter.accept((Feature) i.next())) {
              return true;
            }
          }

          return false;
        }

        public int hashCode() {
            return filter.hashCode() + 173;
        }

        public boolean equals(Object o) {
            if (! (o instanceof FeatureFilter.ByChild)) {
                return false;
            }

            FeatureFilter.ByChild ffbc = (FeatureFilter.ByChild) o;
            return ffbc.getFilter().equals(filter);
        }

        public boolean isProperSubset(FeatureFilter ff) {
            FeatureFilter descFilter = null;
            if (ff instanceof FeatureFilter.ByChild) {
                descFilter = ((FeatureFilter.ByChild) ff).getFilter();
            } else if (ff instanceof FeatureFilter.ByDescendant) {
                descFilter = ((FeatureFilter.ByDescendant) ff).getFilter();
            }

            if (descFilter != null) {
                return FilterUtils.areProperSubset(descFilter, filter);
            } else {
                return false;
            }
        } 

        public boolean isDisjoint(FeatureFilter ff) {
            if (ff instanceof IsLeaf) {
                return true;
            }
            
            FeatureFilter descFilter = null;
            if (ff instanceof FeatureFilter.ByChild) {
                descFilter = ((FeatureFilter.ByChild) ff).getFilter();
            }

            if (descFilter != null) {
                return FilterUtils.areDisjoint(descFilter, filter);
            } else {
                return false;
            }
        }
    }


    /**
     * Filter by applying a nested <code>FeatureFilter</code> to all
     * descendant features.  Returns <code>true</code> if at least one
     * of them matches the filter.  Always <code>false</code> if the
     * feature has no children.
     *
     * @author Matthew Pocock
     * @author Thomas Down
     * @since 1.2
     */

    public static class ByDescendant implements OptimizableFilter, Down {
        private FeatureFilter filter;

        public ByDescendant(FeatureFilter ff) {
            filter = ff;
        }

        public FeatureFilter getFilter() {
            return filter;
        }

        public boolean accept(Feature f) {
            do {
                FeatureHolder fh = f.getParent();
                if (fh instanceof Feature) {
                    f = (Feature) fh;
                    if (filter.accept(f)) {
                        return true;
                    }
                } else {
                    return false;
                }
            } while (true);
        }

        public int hashCode() {
            return filter.hashCode() + 186;
        }

        public boolean equals(Object o) {
            if (! (o instanceof FeatureFilter.ByDescendant)) {
                return false;
            }

            FeatureFilter.ByDescendant ffba = (FeatureFilter.ByDescendant) o;
            return ffba.getFilter().equals(filter);
        }

        public boolean isProperSubset(FeatureFilter ff) {
            FeatureFilter ancFilter = null;
            if (ff instanceof FeatureFilter.ByDescendant) {
                ancFilter = ((FeatureFilter.ByDescendant) ff).getFilter();
            }

            if (ancFilter != null) {
                return FilterUtils.areProperSubset(ancFilter, filter);
            } else {
                return false;
            }
        }

        public boolean isDisjoint(FeatureFilter ff) {
            if (ff instanceof IsTopLevel) {
                return true;
            }
            
            FeatureFilter descFilter = null;
            if (ff instanceof FeatureFilter.ByChild) {
                descFilter = ((FeatureFilter.ByChild) ff).getFilter();
            } else if (ff instanceof FeatureFilter.ByDescendant) {
                descFilter = ((FeatureFilter.ByDescendant) ff).getFilter();
            }

            if (descFilter != null) {
                return FilterUtils.areDisjoint(descFilter, filter);
            } else {
                return false;
            }
        }
    }

  /**
   * Accept features with a given reading frame.
   *
   * @author Mark Schreiber
   * @since 1.2
   */
  public final static class FrameFilter implements OptimizableFilter {
    private FramedFeature.ReadingFrame frame;

    /**
     * Build a new filter that matches all features of a reading frame.
     *
     * @param frame the ReadingFrame to match
     */
    public FrameFilter(FramedFeature.ReadingFrame frame) {
      this.frame = frame;
    }

    /**
     * Retrieve the reading frame this filter matches.
     */
     public FramedFeature.ReadingFrame getFrame(){
       return frame;
     }

    /**
     * Accept the Feature if it is an instance of FramedFeature and matches
     * the value of getFrame().
     *
     * @param f the Feature to check
     * @return true if the frame matches, or false otherwise
     */
    public boolean accept(Feature f) {
      if(f instanceof FramedFeature) {
        FramedFeature ff = (FramedFeature) f;
        return ff.getReadingFrame() == frame;
      } else {
        return false;
      }
    }

    public boolean equals(Object o) {
      return (o instanceof StrandFilter);
    }

    public boolean isProperSubset(FeatureFilter sup) {
      return this.equals(sup);
    }

    public boolean isDisjoint(FeatureFilter filt) {
      return (filt instanceof AcceptNoneFilter) || (
        (filt instanceof FrameFilter) &&
        ((FrameFilter) filt).getFrame() == getFrame()
      );
    }
  }

    /**
     * <code>ByPairwiseScore</code> is used to filter
     * <code>SimilarityPairFeature</code>s by their score. Features
     * are accepted if their score falls between the filter's minimum
     * and maximum values, inclusive. Features are rejected if they
     * are not <code>SimilarityPairFeature</code>s. The minimum value
     * accepted must be less than the maximum value.
     *
     * @author Keith James
     * @since 1.3
     */
    public static final class ByPairwiseScore implements OptimizableFilter {
        private double minScore;
        private double maxScore;
        private double score;
        private int    hashCode;

        /**
         * Creates a new <code>ByPairwiseScore</code>.
         *
         * @param minScore a <code>double</code>.
         * @param maxScore a <code>double</code>.
         */
        public ByPairwiseScore(double minScore, double maxScore) {
            if (minScore > maxScore)
                throw new IllegalArgumentException("Filter minimum score must be less than maximum score");

            this.minScore = minScore;
            this.maxScore = maxScore;

            hashCode += (minScore == 0.0 ? 0L : Double.doubleToLongBits(minScore));
            hashCode += (maxScore == 0.0 ? 0L : Double.doubleToLongBits(maxScore));
        }

        /**
         * Accept a Feature if it is an instance of
         * SimilarityPairFeature and its score is <= filter's minimum
         * score and >= filter's maximum score.
         *
         * @param f a <code>Feature</code>.
         * @return a <code>boolean</code>.
         */
        public boolean accept(Feature f) {
            if (! (f instanceof SimilarityPairFeature)) {
                return false;
            }

            score = ((SimilarityPairFeature) f).getScore();
            return (score >= minScore &&
                    score <= maxScore);
        }

        /**
         * <code>getMinScore</code> returns the minimum score
         * accepted.
         *
         * @return a <code>double</code>.
         */
        public double getMinScore() {
            return minScore;
        }

        /**
         * <code>getMaxScore</code> returns the maximum score
         * accepted.
         *
         * @return a <code>double</code>.
         */
        public double getMaxScore() {
            return maxScore;
        }

        public boolean equals(Object o) {
            if (o instanceof ByPairwiseScore) {
                ByPairwiseScore psf = (ByPairwiseScore) o;
                if (psf.getMinScore() == minScore &&
                    psf.getMaxScore() == maxScore) {
                    return true;
                }
            }
            return false;
        }

        public int hashCode() {
            return hashCode;
        }

        public boolean isProperSubset(FeatureFilter sup) {
            if (sup instanceof ByPairwiseScore) {
                ByPairwiseScore psf = (ByPairwiseScore) sup;
                return (psf.getMinScore() <= minScore &&
                        psf.getMaxScore() >= maxScore);
            }
            return false;
        }

        public boolean isDisjoint(FeatureFilter filt) {
            if (filt instanceof AcceptNoneFilter)
                return true;

            if (filt instanceof ByPairwiseScore) {
                ByPairwiseScore psf = (ByPairwiseScore) filt;
                return (psf.getMaxScore() < minScore ||
                        psf.getMinScore() > maxScore);
            }
            return false;
        }

        public String toString() {
            return minScore + " >= score <= " + maxScore;
        }
    }

    /**
     * Accepts features which are ComponentFeatures and have a <code>componentSequenceName</code>
     * property of the specified value.
     *
     * @author Thomas Down
     * @since 1.3
     */

    public final static class ByComponentName implements OptimizableFilter {
        private String cname;

        public ByComponentName(String cname) {
            this.cname = cname;
        }

        public boolean accept(Feature f) {
            if (f instanceof ComponentFeature) {
                return cname.equals(((ComponentFeature) f).getComponentSequenceName());
            } else {
                return false;
            }
        }

        public String getComponentName() {
            return cname;
        }

        public boolean equals(Object o) {
            return (o instanceof ByComponentName) && ((ByComponentName) o).getComponentName().equals(cname);
        }

        public int hashCode() {
            return getComponentName().hashCode();
        }

        public boolean isProperSubset(FeatureFilter sup) {
            if (sup instanceof ByComponentName) {
                return equals(sup);
            } else if (sup instanceof ByClass) {
                return ((ByClass) sup).getTestClass().isAssignableFrom(ComponentFeature.class);
            } else {    
                return (sup instanceof AcceptAllFilter);
            }
        }

        public boolean isDisjoint(FeatureFilter feat) {
            if (feat instanceof ByComponentName) {
                return !equals(feat);
            } else if (feat instanceof ByClass) {
                Class featC = ((ByClass) feat).getTestClass();
                return ! (featC.isAssignableFrom(ComponentFeature.class));
            } else {
                return (feat instanceof AcceptNoneFilter);
            }
        }

        public String toString() {
            return "ByComponentName(" + cname + ")";
        }
    }
    
    public static final FeatureFilter top_level = new IsTopLevel();

    public static final FeatureFilter leaf = new IsLeaf();
    
    // Note: this implements OptimizableFilter, but cheats :-).  Consequently,
    // other optimizablefilters don't know anything about it.  The convenience
    // methods on FilterUtils give ByFeature a higher precedence to make
    // sure this works out.
    
    /**
     * Accept only features which are equal to the specified feature
     *
     * @author Thomas Down
     * @since 1.3
     */
    
    public static final class ByFeature implements OptimizableFilter {
        private final Feature feature;
        
        public ByFeature(Feature f) {
            this.feature = f;
        }
        
        public Feature getFeature() {
            return feature;
        }
        
        public boolean accept(Feature f) {
            return f.equals(feature);
        }
        
        public boolean isProperSubset(FeatureFilter ff) {
            return ff.accept(feature);
        }
        
        public boolean isDisjoint(FeatureFilter ff) {
            return !ff.accept(feature);
        }
        
        public int hashCode() {
            return feature.hashCode() + 65;
        }
        
        public boolean equals(Object o) {
            if (o instanceof FeatureFilter.ByFeature) {
                return ((FeatureFilter.ByFeature) o).getFeature().equals(feature);
            } else {
                return false;
            }
        }
    }
}

interface ByHierarchy extends FeatureFilter {
  FeatureFilter getFilter();
}

interface Up extends FeatureFilter {}
interface Down extends FeatureFilter {}


/**
 * The class that accepts all features.
 * <p>
 * Use the FeatureFilter.all member.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @since 1.0
 */
class AcceptAllFilter implements OptimizableFilter {
  protected AcceptAllFilter() {}
  
  public boolean accept(Feature f) { return true; }
  
  public boolean equals(Object o) {
    return o instanceof AcceptAllFilter;
  }
  
  public int hashCode() {
    return 0;
  }
  
  public boolean isProperSubset(FeatureFilter sup) {
    return sup instanceof AcceptAllFilter;
  }
  
  public boolean isDisjoint(FeatureFilter filt) {
    return filt instanceof AcceptNoneFilter;
  }
  
  public String toString() {
    return "All";
  }
}

/**
 * The class that accepts no features.
 * <p>
 * Use the FeatureFilter.none member.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
class AcceptNoneFilter implements OptimizableFilter {
  protected AcceptNoneFilter() {}
  
  public boolean accept(Feature f) { return false; }
  
  public boolean equals(Object o) {
    return o instanceof AcceptNoneFilter;
  }
  
  public int hashCode() {
    return 1;
  }
  
  public boolean isProperSubset(FeatureFilter sup) {
    return true;
  }
  
  public boolean isDisjoint(FeatureFilter filt) {
    return true;
  }
  
  public String toString() {
    return "None";
  }
}


/**
 * Accept features which are top-level sequence features.  This is implemented
 * by the logic that the <code>parent</code> property of top-level features
 * will implement the <code>Sequence</code> interface.
 *
 * @author Thomas Down
 * @since 1.3
 */

final class IsTopLevel implements OptimizableFilter {
  public boolean accept(Feature f) {
    return f.getParent() instanceof Sequence;
  }
  
  public int hashCode() {
    return 42;
  }
  
  /**
  * All instances are equal (this should really be a singleton, but
  * that doesn't quite fit current </code>FeatureFilter</code>
  * patterns.
  */
  
  public boolean equals(Object o) {
    return (o instanceof IsTopLevel);
  }
  
  public boolean isProperSubset(FeatureFilter ff) {
    return (ff instanceof IsTopLevel) || (ff instanceof AcceptAllFilter);
  }
  
  public boolean isDisjoint(FeatureFilter ff) {
    return (ff instanceof ByParent) || (ff instanceof ByAncestor);
  }
}

/**
 * Accept features which themselves have no children.
 *
 * @author Matthew Pocock
 * @since 1.3
 */

final class IsLeaf implements OptimizableFilter {
  public boolean accept(Feature f) {
    return f.countFeatures() == 0;
  }
  
  public int hashCode() {
    return 41;
  }
  
  /**
  * All instances are equal (this should really be a singleton, but
  * that doesn't quite fit current </code>FeatureFilter</code>
  * patterns.
  */
  
  public boolean equals(Object o) {
    return (o instanceof IsLeaf);
  }
  
  public boolean isProperSubset(FeatureFilter ff) {
    return (ff instanceof IsLeaf) || (ff instanceof AcceptAllFilter);
  }
  
  public boolean isDisjoint(FeatureFilter ff) {
    return ff instanceof ByChild || ff instanceof ByDescendant || ff instanceof AcceptNoneFilter;
  }
}

