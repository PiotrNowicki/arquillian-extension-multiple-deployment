package com.piotrnowicki.arquillian.extension.examples;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class MultiplePlainDeployment {
	@Deployment
	public static Archive<?> deploy() {
		return ShrinkWrap.create(WebArchive.class);
	}

	@Deployment
	public static Archive<?> deploy2() {
		return ShrinkWrap.create(WebArchive.class);
	}
}
