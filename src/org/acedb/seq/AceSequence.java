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


package org.acedb.seq;

import java.util.*;

import org.acedb.*;
import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.genomic.*;
import org.biojava.bio.seq.impl.*;

/**
 * Sequence extracted from an ACeDB database.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */

public class AceSequence implements Sequence {
  protected AceObject seqObj;
  private String name;
  private SymbolList symList;
  private Annotation annotation;
  private SimpleFeatureHolder fHolder;
  
  public String getName() {
    return name;
  }
  
  public String getURN() {
    return "urn:sequence/acedb:" + getName();
  }
  
  public Alphabet getAlphabet() {
    return DNATools.getDNA();
  }
  
  public Annotation getAnnotation() {
    return annotation;
  }

  public Iterator iterator() {
    return symList.iterator();
  }
  
  public int length() {
    return symList.length();
  }
  
  public Symbol symbolAt(int index) {
    return symList.symbolAt(index);
  }
  
  public List toList() {
    return symList.toList();
  }
  
  public SymbolList subList(int start, int end) {
    return symList.subList(start, end);
  }
  
  public String subStr(int start, int end) {
    return symList.subStr(start, end);
  }
  
  public String seqString() {
    return symList.seqString();
  }
  
  public int countFeatures() {
    return fHolder.countFeatures();
  }
  
  public Iterator features() {
    return fHolder.features();
  }
  
  public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
    return fHolder.filter(ff, recurse);
  }

  /**
   * Add a new feature to this sequence.
   * <P>
   * Ace sequences are currently immutable. This may be changed in the future.
   * This method will always throw an UnsupportedOperationException.
   */
  public Feature createFeature(FeatureHolder fh, Feature.Template template)
  throws UnsupportedOperationException {
    throw new UnsupportedOperationException("ACeDB sequences can't be modified");
  }

  /**
   * Add a new feature to this sequence.
   * <P>
   * Ace sequences are currently immutable. This may be changed in the future.
   * This method will always throw an UnsupportedOperationException.
   */
  public Feature createFeature(Feature.Template template)
  throws UnsupportedOperationException {
    throw new UnsupportedOperationException("ACeDB sequences can't be modified");
  }
   
    public Feature realizeFeature(Feature.Template template) {
	throw new UnsupportedOperationException("Cannot realize new features on ACeDB sequences.");
    }
 
    public void removeFeature(Feature f) {
	throw new UnsupportedOperationException("ACeDB sequences can't be modified");
    }
    
  public AceSequence(AceObject seqObj) throws AceException, BioException {
    this.name = seqObj.getName();
    this.fHolder = new SimpleFeatureHolder();
    
    try {
      // load in relevent ACeDB object & construct annotation wrapper
      annotation = new AceAnnotation(seqObj);
      
      // load in the corresponding dna
      Connection con = Ace.getConnection(seqObj.toURL());
      String selectString = con.transact("Find Sequence " + name);
      String dnaString = con.transact("dna");
      SymbolParser rParser = getAlphabet().getParser("token");
      List rl = new ArrayList();
      StringTokenizer st = new StringTokenizer(dnaString, "\n");
      while(st.hasMoreElements()) {
        String line = st.nextToken();
        if(!line.startsWith(">")) {
          if(line.startsWith("//"))
            break;
          rl.addAll(rParser.parse(line).toList());
        }
      }
      symList = new SimpleSymbolList(getAlphabet(), rl);
      con.dispose();
      
      // Feature template for stuff
      Feature.Template template = new Feature.Template();

      // make features for 'Subsequence' objects
      if(seqObj.contains("Details:")) {
        AceSet dets = seqObj.retrieve("Details:");
        if(dets.contains("Subsequence")) {
          AceSet subSeq = dets.retrieve("Subsequence");
          for(Iterator ssI = subSeq.nameIterator(); ssI.hasNext(); ) {
            String name = (String) ssI.next();
            Reference ref = (Reference) subSeq.retrieve(name);
            IntValue start = (IntValue) AceUtils.pick(ref);
            for(Iterator eI = start.iterator(); eI.hasNext(); ) {
              IntValue end = (IntValue) eI.next();
              Annotation fAnn = new SimpleAnnotation();
              fAnn.setProperty("references", ref);
              template.annotation = fAnn;
              template.location = new RangeLocation(start.toInt(), end.toInt());
              template.source = "ACeDB";
              template.type = name;
              Feature f = FeatureImpl.DEFAULT.realizeFeature(this, this, template);
              fHolder.addFeature(f);
            }
          }
        }
      }
      // make features for each 'Sequence_Feature:' child.
      if(seqObj.contains("Sequence_feature:")) {
        AceSet sf = seqObj.retrieve("Sequence_feature:");
        for(Iterator nameI = sf.nameIterator(); nameI.hasNext(); ) {
          String name = (String) nameI.next();
          AceSet fTypeNode = sf.retrieve(name);
          for(Iterator fI = fTypeNode.nameIterator(); fI.hasNext(); ) {
            AceNode an = (AceNode) fTypeNode.retrieve((String) fI.next());
            IntValue start = (IntValue) an;
            for(Iterator eI = start.iterator(); eI.hasNext(); ) {
              IntValue end = (IntValue) eI.next();
              Annotation fAnn;
              if((end.size() > 0)) {
                StringBuffer comment = new StringBuffer();
                Iterator cI = end.nameIterator();
                comment.append(cI.next());
                while(cI.hasNext())
                  comment.append("\n" + cI.next());
                fAnn = new SimpleAnnotation();
                fAnn.setProperty("description", comment.toString());
              } else {
                fAnn = Annotation.EMPTY_ANNOTATION;
              }
              template.location = new RangeLocation(start.toInt(), end.toInt());
              template.source = "ACeDB";
              template.type = name;
              template.annotation = fAnn;
              Feature f = FeatureImpl.DEFAULT.realizeFeature(this, this, template);
              fHolder.addFeature(f);
            }
          }
        }
      }
    } catch (Exception ex) {
      throw new AceException(ex, "Fatal error constructing sequence for " + name);
    }
  }
  
  public void edit(Edit edit) throws ChangeVetoException {
    throw new ChangeVetoException("Ace sequences are currently immutable");
  }
  
  public void addChangeListener(ChangeListener cl) {}
  public void addChangeListener(ChangeListener cl, ChangeType ct) {}
  public void removeChangeListener(ChangeListener cl) {}
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {} 
}
