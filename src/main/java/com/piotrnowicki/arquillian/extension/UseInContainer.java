package com.piotrnowicki.arquillian.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines that the Deployment should be used only when particular container
 * adapter is active.
 * 
 * @author PedroKowalski
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UseInContainer {

	/**
	 * Name of the container for which the deployment should be used.
	 */
	TargetContainerName value();
}
