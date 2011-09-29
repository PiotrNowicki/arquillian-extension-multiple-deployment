package com.pedrokowalski.arquillian.extension;

import java.util.logging.Logger;

/**
 * Represents possible target container which deployment can be explicitly set
 * in test class.
 * 
 * @author PedroKowalski
 * 
 */
public enum TargetContainerName {

	/**
	 * Glassfish 3.1 container.
	 */
	GLASSFISH("container.glassfish"),

	/**
	 * JBoss AS7 container.
	 */
	JBOSSAS7("org.jboss.as.arquillian.container");

	/**
	 * Holds the unique part of the container adapter package name.
	 */
	private String packageName;

	private static final Logger log = Logger.getLogger(TargetContainerName.class.getName());

	private TargetContainerName(String packageName) {
		this.packageName = packageName;
	}

	public String getPackageName() {
		return packageName;
	}

	/**
	 * Gets the {@link TargetContainerName} basing on the runtime deployment
	 * class passed as an argument.
	 * 
	 * @param clazz
	 *            deployment class for which the target container should be
	 *            returned
	 * @return target container name
	 */
	public static TargetContainerName get(Class<?> clazz) {
		for (TargetContainerName tcn : values()) {
			String packageName = clazz.getPackage().getName().toLowerCase();

			if (packageName.contains(tcn.getPackageName())) {
				return tcn;
			}
		}

		// No matches found - go with the default.
		log.warning("No matching container for '" + clazz.getName()
				+ "' found. Falling back to default.");

		return GLASSFISH;
	}
}
