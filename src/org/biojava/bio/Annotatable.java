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


package org.biojava.bio;

import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeForwarder;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeType;
import org.biojava.utils.Changeable;

/**
 * <p>Indicates that an object has an associated annotation.</p>
 *
 * <p>Many BioJava objects will have associated unstructured
 * data. This should be stored in an Annotation instance. However, the
 * BioJava object itself will probably not want to extend the
 * Annotation interface directly, but rather delegate off that
 * functionality to an Annotation property. The Annotatable interface
 * indicates that there is an Annoation property. When implementing
 * Annotatable, you should always create a protected or private field
 * containing an instance of ChangeForwarder, and register it as a
 * ChangeListener with the associated Annotation delegate
 * instance.</p>
 *
 * <pre>
 * public class Foo extends AbstractChangeable implements Annotatable {
 *   private Annotation ann; // the associated annotation delegate
 *   protected ChangeForwarder annFor; // the event forwarder
 *
 *   public Foo() {
 *     // make the ann delegate
 *     ann = new SimpleAnnotation();
 *     // construct the forwarder so that it emits Annotatable.ANNOTATION ChangeEvents
 *     // for the Annotation.PROPERTY events it will listen for
 *     annFor = new ChangeForwarder.Retyper(this, getChangesupport( Annotatable.ANNOTATION ), 
 *                                          Annotatable.ANNOTATION );
 *     // connect the forwarder so it listens for Annotation.PROPERTY events
 *     ann.addChangeListener( annFor, Annotation.PROPERTY ); 
 *   }
 *
 *   public Annotation getAnnotation() {
 *     return ann;
 *   }
 * }
 * </pre>
 *
 * @author  Matthew Pocock
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a> (docs).
 *
 * @for.user Check if BioJava classes and interfaces extend Annotatable. This
 * will tell  you if you should look for associated annotation.
 *
 * @for.powerUser If an object implements Annotatable, it may well propagate
 * ChangeEvent notifications from the associated Annotation. You may
 * need to track these to maintain the state of your applications.
 *
 * @for.developer Be careful to hook up the appropriate event forwarders.
 *
 * @for.developer The getAnnotation() method can be implemented lazily
 * (instantiate the Annotation instance and event forwarders when the first
 * request comes in). It can also be implemented by returning throw-away
 * immutable Annotatoin instances that are built from scratch each time.
 *
 * @since 1.0
 */
public interface Annotatable extends Changeable {
  /**
   * Signals that the associated Annotation has altered in some way. The
   * chainedEvent property should refer back to the event fired by the
   * Annotation object.
   */
  public static final ChangeType ANNOTATION = new ChangeType(
    "the associated annotation has changed",
    "org.biojava.bio.Annotatable",
    "ANNOTATION"
  );

  /**
   * Should return the associated annotation object.
   *
   * @return an Annotation object, never null
   */
  Annotation getAnnotation();

  /**
   * <p>A helper class so that you don't have to worry about
   * forwarding events from the Annotation object to the Annotatable
   * one.</p>
   *
   * <p>Once a listener is added to your Annotatable that is
   * interested in ANNOTATION events, then instantiate one of these
   * and add it as a listener to the annotation object. It will
   * forward the events to your listeners and translate them
   * accordingly.</p>
   *
   * @author Matthew Pocock
   *
   * @for.developer This will ease the pain of letting your Annotatable tell its
   * listeners about changes in the Annotation.
   *
   * @deprecated use
   *   <code>new ChangeForwarder.Retyper(source, cs, Annotation.PROPERTY)</code>
   *   instead
   */
  static class AnnotationForwarder extends ChangeForwarder {
    /**
     * Create a new AnnotationForwarder that will forward events for a source
     * using a change support.
     *
     * @param source  the Object to forward events on behalf of
     * @param cs      the change support that manages listeners
     */
    public AnnotationForwarder(Object source, ChangeSupport cs) {
      super(source, cs);
    }

    protected ChangeEvent generateEvent(ChangeEvent ce) {
      ChangeType ct = ce.getType();
      if(ct == Annotation.PROPERTY) {
        return new ChangeEvent(
          getSource(),
          ANNOTATION,
          ct
        );
      }
      return null;
    }
  }
}
