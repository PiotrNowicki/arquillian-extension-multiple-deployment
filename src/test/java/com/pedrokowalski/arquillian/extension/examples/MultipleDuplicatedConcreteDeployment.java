package com.pedrokowalski.arquillian.extension.examples;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import com.pedrokowalski.arquillian.extension.TargetContainerName;
import com.pedrokowalski.arquillian.extension.UseInContainer;

public class MultipleDuplicatedConcreteDeployment {
	@Deployment
	@UseInContainer(TargetContainerName.GLASSFISH)
	public static Archive<?> deploy() {
		return ShrinkWrap.create(WebArchive.class);
	}

	@Deployment
	@UseInContainer(TargetContainerName.GLASSFISH)
	public static Archive<?> deploy2() {
		return ShrinkWrap.create(WebArchive.class);
	}
}
