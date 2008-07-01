/*
 * Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.griffon.commons;

import groovy.lang.GroovyClassLoader;
//import groovy.util.ConfigObject;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.core.io.Resource;

import java.util.Map;

/**
 *  <p>The main interface representing a build-time Griffon application. This interface's
 * main purpose is to provide a mechanism for analysing the conventions within a Griffon
 * application as well as providing metadata and information about the execution environment.
 *
 * <p>The GriffonApplication interface interfacts with { @ link org.codehaus.groovy.griffon.commons.ArtefactHandler} instances
 * which are capable of analysing different artefact types (controllers, domain classes etc.) and introspecting
 * the artefact conventions
 *
 * <p>Implementors of this inteface should be aware that a GriffonApplication is only initialised when the initialise() method
 * is called. In other words GriffonApplication instances are lazily initialised by the Griffon runtime.
 *
 * @see #initialise()
 * @ see ArtefactHandler
 *
 * @author Graeme Rocher
 * @author Steven Devijver
 *
 * @since 0.1
 *
 * Created: Jul 2, 2005
 */
public interface GriffonContext { // extends ApplicationContextAware {
    /**
     * The name of the system property whose value contains the location, during development, of the Griffon working directory where temporary files are generated to
     */
    static final String WORK_DIR = "griffon.work.dir";
    /**
     * The name of the system property whose value contains the location, during development, of the current Griffon projects resources directory
     */
    static final String PROJECT_RESOURCES_DIR = "griffon.project.resource.dir";

    /**
     * The name of the system property whose value contains the location, during development, of the current Griffon projects resources directory
     */
    static final String PROJECT_CLASSES_DIR = "griffon.project.class.dir";
    /**
     * The name of the system property whose value contains the location, during development, of the current Griffon projects resources directory
     */
    static final String PROJECT_TEST_CLASSES_DIR = "griffon.project.test.class.dir";


    /**
     * The id of the griffon application within a bean context
     */
    static final String APPLICATION_ID = "griffonApplication";
    /**
     * Constant used to resolve the environment via System.getProperty(ENVIRONMENT)
     */
    static final String ENVIRONMENT = "griffon.env";

    /**
     * Constants that indicates whether this GriffonApplication is running in the default environment
     */
    static final String ENVIRONMENT_DEFAULT = "griffon.env.default";

    /**
     * Constant for the development environment
     */
    static final String ENV_DEVELOPMENT = "development";
    /**
     * Constant for the application data source, primarly for backward compatability for those applications
     * that use ApplicationDataSource.groovy
     */
    static final String ENV_APPLICATION = "application";

    /**
     * Constant for the production environment
     */
    static final String ENV_PRODUCTION = "production";

    /*
     * Constant for the test environment
     */
    static final String ENV_TEST  = "test";

    /**
     * The name of the class that provides configuration
     */
    static final String CONFIG_CLASS = "Config";

    /**
     * Returns the ConfigObject instance
     *
     * @return The ConfigObject instance
     */
//    public ConfigObject getConfig();


    /**
     * <p>Returns the class loader instance for the Griffon application</p>
     *
     * @return The GroovyClassLoader instance
     */
    public GroovyClassLoader getClassLoader();

    /**
     * Retrieves the controller that is scaffolding the specified domain class
     *
     * @param domainClass The domain class to check
     * @return An instance of GriffonControllerClass
     */
//    GriffonControllerClass getScaffoldingController(GriffonDomainClass domainClass);

    /**
     * Retrieves all java.lang.Class instances loaded by the Griffon class loader
     * @return An array of classes
     */
    public Class[] getAllClasses();

    /**
     * Retrieves all java.lang.Class instances considered Artefacts loaded by the Griffon class loader
     * @return An array of classes
     */
//    public Class[] getAllArtefacts();

    /**
     *
     * @return The parent application context
     */
//    ApplicationContext getParentContext();

    /**
     * Retrieves a class for the given name within the GriffonApplication or returns null
     *
     * @param className The name of the class
     * @return The class or null
     */
    public Class getClassForName(String className);


    /**
     * This method will rebuild the constraint definitions
     * @todo move this out? Why ORM dependencies in here?
     */
//    public void refreshConstraints();

    /**
     * This method will refresh the entire application
     */
//    public void refresh();

    /**
     * Rebuilds this Application throwing away the class loader and re-constructing it from the loaded resources again.
     * This method can only be called in development mode and an error will be thrown if called in a different enivronment
     */
//    public void rebuild();

    /**
     * Retrieves a Resource instance for the given Griffon class or null it doesn't exist
     *
     * @param theClazz The Griffon class
     * @return A Resource or null
     */
//    public Resource getResourceForClass(Class theClazz);

    /**
     * <p>Call this to find out if the class you have is an artefact loaded by griffon.</p>
     * @param theClazz A class to test
     * @return True if and only if the class was loaded from griffon-app/
     * @since 0.5
     */
//    public boolean isArtefact(Class theClazz);

    /**
     * <p>Check if the specified artefact Class has been loaded by Griffon already AND is
     * of the type expected</p>
     * @param artefactType A string identifying the artefact type to check for
     * @param theClazz The class to check
     * @return True if Griffon considers the class to be managed as an artefact of the type specified.
     * @since 0.5
     */
//    public boolean isArtefactOfType(String artefactType, Class theClazz);

    /**
     * <p>Check if the artefact Class with the name specified is of the type expected</p>
     * @param artefactType A string identifying the artefact type to check for
     * @param className The name of a class to check
     * @return True if Griffon considers the class to be managed as an artefact of the type specified.
     * @since 0.5
     */
//    public boolean isArtefactOfType(String artefactType, String className);

    /**
     * <p>Gets the GriffonClass associated with the named artefact class</p>
     * <p>i.e. to get the GriffonClass for  controller called "BookController" you pass the name "BookController"</p>
     * @param artefactType The type of artefact to retrieve, i.e. "Controller"
     * @param name The name of an artefact such as "BookController"
     * @return The associated GriffonClass or null
     * @since 0.5
     */
//    public GriffonClass getArtefact(String artefactType, String name);

    /**
     * <p>Obtain all the class information about the artefactType specified</p>
     * @param artefactType An artefact type identifier i.e. "Domain"
     * @return The artefact info or null if the artefactType is not recognized
     * @since 0.5
     */
//    public ArtefactInfo getArtefactInfo(String artefactType);

    /**
     * <p>Get an array of all the GriffonClass instances relating to artefacts of the specified type.</p>
     * @param artefactType The type of artefact to retrieve, i.e. "Controller"
     * @return An array of GriffonClasses which may empty by not null
     * @since 0.5
     */
//    public GriffonClass[] getArtefacts(String artefactType);

    /**
     * <p>Get an artefact GriffonClass by a "feature" which depending on the artefact may be a URI or tag name
     * for example</p>
     * @param artefactType The type ID of the artefact, i.e. "TagLib"
     * @param featureID The "feature" ID, say a URL or tag name
     * @return The griffon class or null if none is found
     * @since 0.5
     */
//    public GriffonClass getArtefactForFeature(String artefactType, Object featureID);

    /**
     * <p>Registers a new artefact</p>
     * @param artefactType The type ID of the artefact, i.e. "TagLib"
     * @param artefactClass The class of the artefact. A new GriffonClass will be created automatically and added
     * to internal structures, using the appropriate ArtefactHandler
     * @return The new griffon class for the artefact class
     * @since 0.5
     */
//    public GriffonClass addArtefact(String artefactType, Class artefactClass);

    /**
     * <p>Registers a new artefact</p>
     * @param artefactType The type ID of the artefact, i.e. "TagLib"
     * @param artefactGriffonClass The GriffonClass of the artefact.
     * @return The supplied griffon class for the artefact class
     * @since 0.5
     */
//    public GriffonClass addArtefact(String artefactType, GriffonClass artefactGriffonClass);

    /**
     * <p>Register a new artefact handler</p>
     * @param handler The new handler to add
     */
//    public void registerArtefactHandler(ArtefactHandler handler);

    /**
     * <p>Obtain a list of all the artefact handlers</p>
     * @return The list, possible empty but not null, of all currently registered handlers
     */
//    public ArtefactHandler[] getArtefactHandlers();

    /**
     * Initialise this GriffonApplication
     */
    public void initialise();

    /**
     * Returns whether this GriffonApplication has been initialised or not
     * @return True if it has been initialised
     */
    public boolean isInitialised();

    /**
     * <p>Get access to the project's metadata, specified in application.properties</p>
     * <p>This provides access to information like required griffon version, application name, version etc
     * but <b>NOT</b> general application settings.</p>
     * @return A read-only Map of data about the application, not environment specific
     */
    public Map getMetadata();

    /**
     * Retrieves an artefact by its logical property name. For example the logical property name of BookController would be book
     * @param type The artefact type
     * @param logicalName The logical name
     * @return The GriffonClass or null if it doesn't exist
     */
//    GriffonClass getArtefactByLogicalPropertyName(String type, String logicalName);

    /**
     * Adds the given artefact, attempting to determine type from
     * @param artefact The artefact to add
     */
//    void addArtefact(Class artefact);

    /**
     * Returns true if this application has been deployed as a WAR file
     *
     * @return True if the application is WAR deployed
     */
//    boolean isWarDeployed();
}
