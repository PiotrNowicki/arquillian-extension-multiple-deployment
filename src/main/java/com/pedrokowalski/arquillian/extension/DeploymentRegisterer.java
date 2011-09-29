package com.pedrokowalski.arquillian.extension;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentScenarioGenerator;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Registers the {@link FlexibleRegisterDeployment} which allows to choose the
 * {@link Deployment} annotated method from the test class which will be used
 * for particular target container.
 * 
 * @author PedroKowalski
 * 
 */
public class DeploymentRegisterer implements LoadableExtension {

	@Override
	public void register(ExtensionBuilder builder) {
		builder.service(DeploymentScenarioGenerator.class, FlexibleRegisterDeployment.class);
	}

	/**
	 * Chooses the appropriate {@link Deployment} annotated method basing on the
	 * chosen target container. The target container is defined using
	 * {@link UseInContainer} annotation.
	 * 
	 * If there is only one {@link Deployment} annotated method, it will be
	 * chosen by default. It's assumed that no container-specific deployment
	 * means that no special steps are need to be taken for different
	 * containers.
	 * 
	 * @see DeploymentRegisterer
	 * @see TargetContainerName
	 * @see UseInContainer
	 * 
	 * @author PedroKowalski
	 * 
	 */
	public static class FlexibleRegisterDeployment implements DeploymentScenarioGenerator {

		/**
		 * We need to know what container are we actually using.
		 */
		@Inject
		Instance<ContainerRegistry> container;

		private static final Logger log = Logger.getLogger(FlexibleRegisterDeployment.class
				.getName());

		@Override
		public List<DeploymentDescription> generate(TestClass testClass) {
			Method m = getMethodForDeployment(testClass, getTargetContainer());

			WebArchive deployment;

			try {
				deployment = (WebArchive) m.invoke(testClass);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}

			return Arrays.asList(new DeploymentDescription("On-the-fly deployment", deployment));
		}

		/**
		 * Gets the deployment method for particular target container.
		 * 
		 * @param clazz
		 *            test class which contains {@link Deployment} annotated
		 *            method(s).
		 * @param targetContainer
		 *            target container adapter which is active in classpath.
		 * @return deployment method to be invoked for specified target
		 *         container.
		 */
		private Method getMethodForDeployment(TestClass clazz, TargetContainerName targetContainer) {
			Method[] methods = clazz.getMethods(Deployment.class);

			if (methods.length == 0) {
				throw new IllegalStateException("At least one @Deployment method is required");
			} else {
				for (Method method : methods) {
					if (method.getAnnotation(UseInContainer.class).value().equals(targetContainer)) {
						return method;
					}
				}

				MessageFormat msg = new MessageFormat(
						"Deployment method cannot be found. Are you sure you have an appropriate @UseInContainer annotation on one of your @Deployment methods in {1}?");

				throw new IllegalStateException(msg.format(new Object[] { targetContainer,
						clazz.getName() }));
			}
		}

		/**
		 * Gets the target container which is in user's classpath and is used in
		 * the test run.
		 * 
		 * If no value is specified, the default container (
		 * {@link TargetContainerName#GLASSFISH}) will be chosen.
		 * 
		 * @return name of the target container to be used.
		 */
		private TargetContainerName getTargetContainer() {
			DeployableContainer<?> dc = container.get().getContainers().get(0)
					.getDeployableContainer();

			TargetContainerName result = TargetContainerName.get(dc.getClass());

			log.info("Using '" + result + "' as a target container.");

			return result;
		}
	}
}
