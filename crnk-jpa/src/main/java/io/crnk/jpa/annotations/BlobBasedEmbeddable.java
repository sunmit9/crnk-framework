package io.crnk.jpa.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * In case of a missing @Embeddable annotation, Hibernate will serialize an object as blob. This annotation
 * will make it still work with Crnk again. Should be considered more of a work around for wrongly
 * setup database schemas that cannot be changed anymore.
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface BlobBasedEmbeddable {

}
