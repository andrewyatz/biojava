package org.biojava.bio.seq.filter;

import org.biojava.bio.seq.filter.Visitor;
import org.biojava.bio.seq.FeatureFilter;

/**
 * Objects that can walk over a filter expression, showing each element to a
 * visitor.
 *
 * @for.user
 * You should use FilterUtils.visitFilter to apply a visitor to a feature
 * filter.
 *
 * @for.powerUser
 * You can use WalkerFactory.getInstance().getWalker(visitor) to get a walker
 * that is suitable for your visitor implementation. This will take care of
 * all the magic needed to hook up visitor call-back methods to the walkers
 * traversal of the features.
 *
 * @for.developer
 * If you don't like the walkers that WalkerFactory produces, you can implement
 * this directly. This will work fine for simple visitors, e.g., which only have
 * a single method for visting all filters, regardless of type.
 *
 * @author Matthew Pocock
 */
public interface Walker {
  /**
   * This walks the feature tree, showing the visitor each filter in
   * the expression.
   *
   * @param filter
   * @param visitor
   */
  public void walk(FeatureFilter filter, Visitor visitor);

  /**
   * If the visitor has a return value, then the result of applying the visitor
   * to the tree can be obtained using this method, otherwise the result will
   * be null.
   *
   * @return the visitor's return value, or null
   */
  public Object getValue();
}
