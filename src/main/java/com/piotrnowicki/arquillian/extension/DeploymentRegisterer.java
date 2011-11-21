package com.piotrnowicki.arquillian.extension;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Registers the {@link FlexibleRegisterDeployment} which allows to choose the {@link Deployment}
 * annotated method from the test class which will be used for particular target container.
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
	 * <p>
	 * Chooses the appropriate {@link Deployment} annotated method basing on the chosen target
	 * container. The target container is defined using {@link UseInContainer} annotation.
	 * </p>
	 * 
	 * <p>
	 * If there is only one {@link Deployment} annotated method, it will be chosen by default. It's
	 * assumed that no container-specific deployment means that no special steps are need to be
	 * taken for different containers.
	 * </p>
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

		/**
		 * Deployment annotated method chosen for the deployment.
		 */
		Method deployment;

		private static final Logger log = Logger.getLogger(FlexibleRegisterDeployment.class
				.getName());

		@Override
		public List<DeploymentDescription> generate(TestClass testClass) {
			deployment = getMethodForDeployment(testClass, getTargetContainer());

			try {
				WebArchive result = (WebArchive) deployment.invoke(testClass);

				return Arrays.asList(new DeploymentDescription("On-the-fly deployment", result));
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}

		/**
		 * <p>
		 * Gets the deployment method for target container.
		 * </p>
		 * <p>
		 * If the target container specific deployment is not available, the default one (if
		 * possible to determine) will be chosen.
		 * </p>
		 * 
		 * @param clazz
		 *            test class which contains {@link Deployment} annotated method(s).
		 * @param targetContainer
		 *            target container adapter which is active in the classpath.
		 * 
		 * @return deployment method to be invoked.
		 */
		Method getMethodForDeployment(TestClass clazz, TargetContainerName targetContainer) {
			Map<TargetContainerName, List<Method>> deploymentMethods = getAllDeploymentMethods(clazz);

			if (deploymentMethods.size() == 0) {
				MessageFormat msg = new MessageFormat(
						"No deployment method can be found. Are you sure you have a @Deployment annotated method in {0}?");

				throw new IllegalStateException(msg.format(new Object[] { clazz.getName() }));
			}

			// We have found the requested container-specific method.
			if (deploymentMethods.containsKey(targetContainer)) {
				List<Method> containerDeploymentsMethods = deploymentMethods.get(targetContainer);

				// We have more than one container-specific methods and we don't know what to do in
				// such situation.
				if (containerDeploymentsMethods.size() > 1) {
					MessageFormat msg = new MessageFormat(
							"More than one \"{0}\" deployment method found.");
					throw new IllegalStateException(msg.format(new Object[] { targetContainer }));
				} else {
					return containerDeploymentsMethods.get(0);
				}
			}

			// Failsafe. No container-specific method defined - try the default one.
			if (deploymentMethods.containsKey(TargetContainerName.NONE)) {
				List<Method> defaultDeploymentsMethods = deploymentMethods
						.get(TargetContainerName.NONE);

				// We have more than one default methods and we don't know what to do in such
				// situation.
				if (defaultDeploymentsMethods.size() > 1) {
					throw new IllegalStateException(
							"More than one default deployment method found.");
				} else {
					return defaultDeploymentsMethods.get(0);
				}
			}

			// This should never occur.
			throw new IllegalStateException("Something went totally wrong...");
		}

		/**
		 * Returns all deployment methods and their target containers defined for the given test
		 * class.
		 * 
		 * @param clazz
		 *            test class for which the deployment methods will be parsed.
		 * 
		 * @return all deployment methods for the <code>clazz</code>
		 */
		Map<TargetContainerName, List<Method>> getAllDeploymentMethods(TestClass clazz) {
			Map<TargetContainerName, List<Method>> result = new HashMap<TargetContainerName, List<Method>>();

			Method[] methods = clazz.getMethods(Deployment.class);

			for (Method method : methods) {
				UseInContainer uicAnnotation = method.getAnnotation(UseInContainer.class);

				TargetContainerName tcn;

				if (uicAnnotation == null) {
					// Default non-container-specific @Deployment.
					tcn = TargetContainerName.NONE;
				} else {
					tcn = uicAnnotation.value();
				}

				if (!result.containsKey(tcn)) {
					result.put(tcn, new ArrayList<Method>());
				}

				result.get(tcn).add(method);
			}

			return result;
		}

		/**
		 * <p>
		 * Gets the target container basing on the one available in the user's classpath.
		 * </p>
		 * 
		 * <p>
		 * If no value is specified, the default non-container-specific will be chosen.
		 * </p>
		 * 
		 * @return the target container to be used.
		 */
		TargetContainerName getTargetContainer() {
			DeployableContainer<?> dc = getContainerInClasspath();
			String packageName = dc.getClass().getPackage().getName().toLowerCase();

			TargetContainerName result = TargetContainerName.get(packageName);

			log.info("Using '" + result + "' as a target container.");

			return result;
		}

		/**
		 * Gets the target container which is in the user's classpath and is used in the test run.
		 * 
		 * @return current container adapter
		 */
		private DeployableContainer<?> getContainerInClasspath() {
			if (container == null || container.get().getContainers().get(0) == null) {
				throw new IllegalStateException(
						"Cannot find any container adapter in the classpath.");
			}

			DeployableContainer<?> result = container.get().getContainers().get(0)
					.getDeployableContainer();

			return result;
		}
	}
}
