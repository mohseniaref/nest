package com.bc.ceres.core.runtime;

import com.bc.ceres.core.CoreException;

/**
 * A configuration element of an extension.
 * <p>This interface also provides a way to create executable extension objects.</p>
 * <p/>
 * This interface is not intended to be implemented by clients.</p>
 */
public interface ConfigurationElement extends ConfigurationElementBase<ConfigurationElement> {

    /**
     * Gets the corresponding shema element, if this is an element of an extension configuration.
     *
     * @return The shema element, or {@code null} if this is a shema element.
     *
     * @see #getDeclaringExtension()
     */
    ConfigurationShemaElement getShemaElement();

    /**
     * Gets the declaring extension, if this is an element of an extension configuration.
     *
     * @return The declaring extension, or {@code null} if this is a shema element.
     *
     * @see #getShemaElement()
     */
    Extension getDeclaringExtension();

    /**
     * Creates and returns a new instance of the executable extension identified by
     * the named attribute of this configuration element. The named attribute value must
     * contain a fully qualified name of a Java class implementing the executable extension.
     * <p/>
     * <p>The specified class is instantiated using its 0-argument public constructor.
     * If the specified class implements the {@link ConfigurableExtension} interface, its
     * {@link ConfigurableExtension#configure configure} method is called, passing to the
     * object the configuration information that was used to create it.</p>
     *
     * @param extensionType the expected type of the executable extension instance
     *
     * @return the executable instance
     *
     * @throws CoreException    if an instance of the executable extension could not be created for any reason.
     * @throws RuntimeException if this is a shema element
     */
    <T> T createExecutableExtension(Class<T> extensionType) throws CoreException;
}
