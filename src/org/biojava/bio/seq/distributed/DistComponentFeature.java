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

package org.biojava.bio.seq.distributed;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;

/**
 * ComponentFeature implementation used by MetaDAS.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @since 1.2
 */

class DistComponentFeature
  extends
    Unchangeable
  implements
    ComponentFeature
{
    private DistributedSequence sequence;
    private FeatureHolder projectedFeatures;
    private Location location;
    private String type;
    private String source;
    private Annotation annotation;
    private StrandedFeature.Strand strand;
    private String componentSequenceName;
    private Sequence componentSequence;
    private Location componentLocation;
    private int translation;

    public DistComponentFeature(DistributedSequence parent,
				ComponentFeature.Template temp)
        throws BioException
    {
      if(temp.location == null) {
        throw new NullPointerException("Template has null location: " + temp);
      }
      
      if(temp.componentLocation ==  null) {
        throw new NullPointerException("Template has null component location: " + temp);
      }
      
	if (locationContent(temp.location) != 
	    locationContent(temp.componentLocation))
	{
	    throw new BioException("Component and container locations must contain an equal number of symbols.");
	}

	if (!temp.location.isContiguous() || !temp.componentLocation.isContiguous()) {
	    throw new BioException("Can only include contiguous segments in an assembly [may change in future]");
	}
	
	this.sequence = parent;
	
	this.location = temp.location;
	this.type = temp.type;
	this.source = temp.source;
	this.annotation = temp.annotation;

	this.strand = temp.strand;
	
	this.componentSequenceName = temp.componentSequenceName;
	this.componentSequence = temp.componentSequence;
	this.componentLocation = temp.componentLocation;

	if (temp.strand == StrandedFeature.NEGATIVE) {
	    this.translation = temp.location.getMax() + temp.componentLocation.getMin();
	} else if (temp.strand == StrandedFeature.POSITIVE) {
	    this.translation = temp.location.getMin() - temp.componentLocation.getMin();
	} else {
	    throw new BioException("Strand must be specified when creating a ComponentFeature");
	}
    }

    public Feature.Template makeTemplate() {
	ComponentFeature.Template cft = new ComponentFeature.Template();
	cft.location = getLocation();
	cft.type = getType();
	cft.source = getSource();
	cft.annotation = getAnnotation();
	cft.strand = getStrand();
	cft.componentSequenceName = getComponentSequenceName();
	cft.componentLocation = getComponentLocation();
	return cft;
    }
    
    private int locationContent(Location l) {
	if (l.isContiguous())
	    return l.getMax() - l.getMin() + 1;
	int content = 0;
	for (Iterator i = l.blockIterator(); i.hasNext(); ) {
	    Location sl = (Location) i.next();
	    content += (sl.getMax() - sl.getMin() + 1);
	}
	return content;
    }

    public boolean isComponentResolvable() {
	return true; // we hope...
    }

    public String getComponentSequenceName() {
	return getComponentSequence().getName();
    }

    public StrandedFeature.Strand getStrand() {
	return strand;
    }

    public Location getLocation() {
	return location;
    }
    
    public void setLocation(Location loc)
    throws ChangeVetoException {
      throw new ChangeVetoException(
        new ChangeEvent(this, LOCATION, loc, this.location),
        "Can't change location as it is immutable"
      );
    }

    public FeatureHolder getParent() {
	return sequence;
    }

    public Sequence getSequence() {
	return sequence;
    }

    public String getSource() {
	return source;
    }

    public void setSource(String source)
    throws ChangeVetoException {
      throw new ChangeVetoException(
        new ChangeEvent(this, TYPE, source, this.source),
        "Can't change source as it is immutable"
      );
    }

    public String getType() {
	return type;
    }

    public void setType(String type)
    throws ChangeVetoException {
      throw new ChangeVetoException(
        new ChangeEvent(this, TYPE, type, this.type),
        "Can't change type as it is immutable"
      );
    }

    public Annotation getAnnotation() {
	return annotation;
    }

    public SymbolList getSymbols() {
	SymbolList syms = componentLocation.symbols(getComponentSequence());
	return syms;
    }

    public Sequence getComponentSequence() {
	if (componentSequence == null) {
	    try {
		componentSequence = sequence.getSequenceDB().getSequence(componentSequenceName);
	    } catch (BioException ex) {
		throw new BioRuntimeException(ex);
	    }
	}

	return componentSequence;
    }

    public Location getComponentLocation() {
	return componentLocation;
    }

    protected FeatureHolder getProjectedFeatures() {
	if (projectedFeatures == null) {
  	    projectedFeatures = new ProjectedFeatureHolder(getComponentSequence(),
  							   this, 
							   translation,
							   getStrand() == StrandedFeature.NEGATIVE);
  	}
	return projectedFeatures;
    }

    public int countFeatures() {
	return componentSequence.countFeatures();
    }

    public Iterator features() {
	return getProjectedFeatures().features();
    }

    public boolean containsFeature(Feature f) {
      return getProjectedFeatures().containsFeature(f);
    }
    
    public FeatureHolder filter(FeatureFilter ff) {
        if (FilterUtils.areDisjoint(ff, new FeatureFilter.ByParent(new FeatureFilter.ByClass(ComponentFeature.class)))) {
            return FeatureHolder.EMPTY_FEATURE_HOLDER;
        } 
	    
        Location loc = FilterUtils.extractOverlappingLocation(ff);
        if (loc != null && !LocationTools.overlaps(loc, getLocation())) {
            // They're looking in another direction.  That's good.
            return FeatureHolder.EMPTY_FEATURE_HOLDER;
        }

        return getProjectedFeatures().filter(ff);
    }
    
    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
        if (FilterUtils.areDisjoint(ff, new FeatureFilter.ByParent(new FeatureFilter.ByClass(ComponentFeature.class)))) {
            return FeatureHolder.EMPTY_FEATURE_HOLDER;
        } 
	    
        Location loc = FilterUtils.extractOverlappingLocation(ff);
        if (loc != null && !LocationTools.overlaps(loc, getLocation())) {
            // They're looking in another direction.  That's good.
            return FeatureHolder.EMPTY_FEATURE_HOLDER;
        }

        return getProjectedFeatures().filter(ff, recurse);
    }

    public Feature createFeature(Feature.Template temp)
        throws BioException
    {
	throw new BioException("Can't create features in a ComponentFeature (yet?)");
    }

    public void removeFeature(Feature f)
    {
	throw new UnsupportedOperationException("Can't remove features from a ComponentFeature.");
    }
}
