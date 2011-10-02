package com.pedrokowalski.arquillian.extension;

import java.util.logging.Logger;

/**
 * Represents possible target container which deployment can be explicitly set in test class.
 * 
 * @author PedroKowalski
 * 
 */
public enum TargetContainerName {

	/**
	 * Glassfish 3.1 container.
	 */
	GLASSFISH("org.jboss.arquillian.container.glassfish"),

	/**
	 * JBoss AS7 container.
	 */
	JBOSSAS7("org.jboss.as.arquillian.container"),

	/**
	 * Open WebBeans container.
	 */
	OPENWEBBEANS("org.jboss.arquillian.container.openwebbeans"),

	/**
	 * Weld container.
	 */
	WELD("org.jboss.arquillian.container.weld.ee"),

	/**
	 * Tomcat 5.5, 6 and 7 container.
	 */
	TOMCAT("org.jboss.arquillian.container.tomcat"),

	/**
	 * Jetty container.
	 */
	JETTY("org.jboss.arquillian.container.jetty"),

	/**
	 * Openshift container.
	 */
	OPENSHIFT("org.jboss.arquillian.container.openshift"),

	/**
	 * Failsafe; if no specific container is required.
	 */
	NONE("");

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
	 * Gets the {@link TargetContainerName} basing on the runtime deployment container package name.
	 * 
	 * @param containerPackage
	 *            name of the container deployment package for which the target container should be
	 *            returned
	 * @return target container name
	 */
	public static TargetContainerName get(String containerPackage) {
		for (TargetContainerName tcn : values()) {
			if (containerPackage.contains(tcn.getPackageName())) {
				return tcn;
			}
		}

		// No matches found - go with the default.
		log.warning("No matching container for '" + containerPackage
				+ "' found. Falling back to default.");

		return NONE;
	}
}
