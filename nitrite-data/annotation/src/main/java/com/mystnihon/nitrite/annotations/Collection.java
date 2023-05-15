package com.mystnihon.nitrite.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * An annotation to add to the object use for the collection in order to trigger the traversal of the attributes of the collection.
 * </p>
 * <p>
 * Ex:
 * For the collection ThisCollection
 * <pre>
 *    {@literal @}Collection
 *     class ThisCollection implements Serializable {
 *
 *        {@literal @}Id
 *         private long id;
 *         private String name;
 *     }
 * </pre>
 *
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Collection {
}
