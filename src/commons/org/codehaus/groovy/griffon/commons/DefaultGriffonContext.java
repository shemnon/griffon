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

import griffon.util.GriffonUtil;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObjectSupport;
import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.codehaus.groovy.control.CompilationFailedException;
//import org.codehaus.groovy.control.CompilerConfiguration;
//import org.codehaus.groovy.griffon.commons.spring.GriffonResourceHolder;
//import org.codehaus.groovy.griffon.commons.cfg.ConfigurationHelper;
//import org.codehaus.groovy.griffon.compiler.GriffonClassLoader;
//import org.codehaus.groovy.griffon.compiler.injection.ClassInjector;
//import org.codehaus.groovy.griffon.compiler.injection.DefaultGriffonDomainClassInjector;
//import org.codehaus.groovy.griffon.compiler.injection.GriffonAwareClassLoader;
//import org.codehaus.groovy.griffon.compiler.support.GriffonResourceLoader;
//import org.codehaus.groovy.griffon.exceptions.GriffonConfigurationException;
//import org.codehaus.groovy.runtime.DefaultGroovyMethods;
//import org.codehaus.groovy.runtime.StackTraceUtils;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.BeanClassLoaderAware;
//import org.springframework.context.ApplicationContext;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.Resource;
//import org.springframework.util.Assert;

//import java.io.IOException;
//import java.lang.reflect.Modifier;
import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

/**
 * Default implementation of the GriffonApplication interface that manages application loading,
 * state, and artefact instances.
 * <p/>
 * Upon loading this GriffonApplication will inspect each class using its registered ArtefactHandler instances. Each
 * ArtefactHandler provides knowledge about the conventions used to establish its artefact type. For example
 * controllers use the ControllerArtefactHandler to establish this knowledge.
 * <p/>
 * New ArtefactHandler instances can be registered with the GriffonApplication thus allowing application extensibility.
 *
 * @author Marc Palmer
 * @author Steven Devijver
 * @author Graeme Rocher
 *
 * @ see org.codehaus.groovy.griffon.plugins.GriffonPluginManager
 * @ see org.codehaus.groovy.griffon.plugins.DefaultGriffonPluginManager
 * @ see org.codehaus.groovy.griffon.commons.ArtefactHandler
 * @ see org.codehaus.groovy.griffon.commons.ArtefactInfo
 * @ since 0.1
 *        <p/>
 *        Created: Jul 2, 2005
 */
public class DefaultGriffonContext extends GroovyObjectSupport implements GriffonContext {//, BeanClassLoaderAware {

    //private static final Pattern GETCLASSESPROP_PATTERN = Pattern.compile("(\\w+)(Classes)");
    //private static final Pattern GETCLASSESMETH_PATTERN = Pattern.compile("(get)(\\w+)(Classes)");
    //private static final Pattern ISCLASS_PATTERN = Pattern.compile("(is)(\\w+)(Class)");
    //private static final Pattern GETCLASS_PATTERN = Pattern.compile("(get)(\\w+)Class");
//    private static final String PROJECT_META_FILE = "application.properties";
    private static final String DATA_SOURCE_CLASS = "DataSource";

    private GroovyClassLoader cl = null;

    private Class[] allClasses = new Class[0];
    private static Log log = LogFactory.getLog(DefaultGriffonContext.class);
//    private ApplicationContext parentContext;

    private Set<Class> loadedClasses = new HashSet<Class>();
//    private GriffonResourceLoader resourceLoader;
    //private ArtefactHandler[] artefactHandlers;
//    private Map artefactHandlersByName = new HashMap();
//    private Set allArtefactClasses = new HashSet();
//    private Map artefactInfo = new HashMap();
//    private boolean suspectArtefactInit;
//    private Class[] allArtefactClassesArray;
    private Map<Object, Object> applicationMeta;
//    private Resource[] resources;
    private boolean initialised = false;
//    private ClassLoader beanClassLoader;
    private static final String CONFIG_BINDING_USER_HOME = "userHome";
    private static final String CONFIG_BINDING_GRIFFON_HOME = "griffonHome";
    private static final String CONFIG_BINDING_APP_NAME = "appName";
    private static final String CONFIG_BINDING_APP_VERSION = "appVersion";
//    private static final String META_GRIFFON_WAR_DEPLOYED = "griffon.war.deployed";

    /**
     * Creates a new empty Griffon application
     */
    public DefaultGriffonContext() {
        this.cl = new GroovyClassLoader();
    }

    /**
     * Creates a new GriffonApplication instance using the given classes and GroovyClassLoader
     *
     * @param classes     The classes that make up the GriffonApplication
     * @param classLoader The GroovyClassLoader to use
     */
    public DefaultGriffonContext(final Class[] classes, GroovyClassLoader classLoader) {
        if (classes == null) {
            throw new IllegalArgumentException("Constructor argument 'classes' cannot be null");
        }

        loadedClasses.addAll(Arrays.asList(classes));
        this.allClasses = classes;
        this.cl = classLoader;
        this.applicationMeta = loadMetadata();
    }


    /* *
     * Constructs a GriffonContext with the given set of groovy sources specified as Spring Resource instances
     *
     * @param resources An array or Groovy sources provides by Spring Resource instances
     * @throws IOException Thrown when an error occurs reading a Groovy source
     */
//    public DefaultGriffonContext(final Resource[] resources) throws IOException {
//        this(new GriffonResourceLoader(resources));
//    }


//    public DefaultGriffonContext(GriffonResourceLoader resourceLoader) {
//        this.resourceLoader = resourceLoader;

//        try {
//            this.applicationMeta = loadMetadata();
//            loadGriffonApplicationFromResources(resourceLoader.getResources());
//        } catch (IOException e) {
//            throw new GriffonConfigurationException("I/O exception loading Griffon: " + e.getMessage(), e);
//        }
//    }

//    private void loadGriffonApplicationFromResources(Resource[] resources) throws IOException {
//        GriffonResourceHolder resourceHolder = new GriffonResourceHolder();
//        this.cl = configureClassLoader(resourceLoader);
//
//        Collection loadedResources = new ArrayList();
//        this.loadedClasses = new HashSet();
//
//        try {
//            for (int i = 0; resources != null && i < resources.length; i++) {
//
//                if (!loadedResources.contains(resources[i])) {
//                    try {
//                        String className = resourceHolder.getClassName(resources[i]);
//                        log.debug("Loading groovy file from resource loader :[" + resources[i].getFile().getAbsolutePath() + "] with name [" + className + "]");
//                        if (!StringUtils.isBlank(className)) {
//
//                            Class c = cl.loadClass(className, true, false);
//                            Assert.notNull(c, "Groovy Bug! GCL loadClass method returned a null class!");
//
//
//                            loadedClasses.add(c);
//                            log.debug("Added Groovy class [" + c + "] to loaded classes");
//                            loadedResources = resourceLoader.getLoadedResources();
//                        }
//                    }
//                    catch (ClassNotFoundException e) {
//                        log.error("The class ["+e.getMessage()+"] was not found when attempting to load Griffon application. Skipping.");
//                    }
//                } else {
//                    Class c = null;
//                    try {
//                        log.debug("Loading groovy file from class loader :[" + resources[i].getFile().getAbsolutePath() + "]");
//                        c = cl.loadClass(resourceHolder.getClassName(resources[i]));
//                    }
//                    catch (ClassNotFoundException e) {
//                        GriffonUtil.deepSanitize(e);
//                        log.error("Class not found attempting to load class " + e.getMessage(), e);
//                    }
//
//                    if (c != null)
//                        loadedClasses.add(c);
//                    log.debug("Added Groovy class [" + c + "] to loaded classes");
//                }
//            }
//        }
//        catch (CompilationFailedException e) {
//            if (GriffonUtil.isDevelopmentEnv()) {
//                // if we're in the development environement then there is no point in this exception propagating up the stack as it
//                // just clouds the actual error so log it as fatal and kill the server
//                log.fatal("Compilation error loading Griffon application: " + e.getMessage(), e);
//                System.exit(1);
//            } else {
//                throw e;
//            }
//        }
//    }

    protected Map<Object, Object> loadMetadata() {
        final Properties meta = new Properties();
//        Resource r = new ClassPathResource(PROJECT_META_FILE);
//        try {
//            meta.load(r.getInputStream());
//        }
//        catch (IOException e) {
//            StackTraceUtils.deepSanitize(e);
//            log.warn("No application metadata file found at " + r);
//        }
        if (System.getProperty(GriffonContext.ENVIRONMENT) != null) {
            meta.setProperty(GriffonContext.ENVIRONMENT, System.getProperty(GriffonContext.ENVIRONMENT));
        }
        return Collections.unmodifiableMap(meta);
    }

    /**
     * Initialises the default set of ArtefactHandler instances
     *
     * @ see org.codehaus.groovy.griffon.commons.ArtefactHandler
     */
//    private void initArtefactHandlers() {
//        registerArtefactHandler(new DomainClassArtefactHandler());
//        registerArtefactHandler(new ControllerArtefactHandler());
//        registerArtefactHandler(new ServiceArtefactHandler());
//        registerArtefactHandler(new TagLibArtefactHandler());
//        registerArtefactHandler(new BootstrapArtefactHandler());
//        registerArtefactHandler(new CodecArtefactHandler());
//        registerArtefactHandler(new UrlMappingsArtefactHandler());
//
//        // Cache the list as an array
//        this.artefactHandlers = ((ArtefactHandler[]) this.artefactHandlersByName.values().toArray(
//                new ArtefactHandler[artefactHandlersByName.size()]));
//    }

    /**
     * Configures a GroovyClassLoader for the given GriffonInjectionOperation and GriffonResourceLoader
     *
     * @param resourceLoader The GriffonResourceLoader
     * @return A GroovyClassLoader
     */
//    private GroovyClassLoader configureClassLoader(
//            final GriffonResourceLoader resourceLoader) {
//
//        final ClassLoader contextLoader =  Thread.currentThread().getContextClassLoader();
//
//        CompilerConfiguration config = CompilerConfiguration.DEFAULT;
//        config.setSourceEncoding("UTF-8");
//
//        ClassLoader rootLoader = DefaultGroovyMethods.getRootLoader(contextLoader);
//        //ClassLoader parentLoader = beanClassLoader != null ? beanClassLoader : contextLoader;
//        GroovyClassLoader cl;
//        if (rootLoader != null) {
//            // This is for when we are using run-app
//
//            // don't support class re-loading now
//            //cl = new GriffonClassLoader(contextLoader, config, resourceLoader);
//            cl = new GroovyClassLoader(contextLoader, config);
//        } else {
//            // This is when we are in WAR
//
//            // don't use injection, rely on ASTTransforms
//            //GriffonAwareClassLoader gcl = new GriffonAwareClassLoader(contextLoader, config);
//            //if (resourceLoader != null)
//            //    gcl.setResourceLoader(resourceLoader);
//            //gcl.setClassInjectors(new ClassInjector[]{new DefaultGriffonDomainClassInjector()});
//            //cl = gcl;
//
//            cl = new GroovyClassLoader(contextLoader, config);
//        }
//
//
//        Thread.currentThread().setContextClassLoader(cl);
//
//        return cl;
//
//    }

    /**
     * Returns all the classes identified as artefacts by ArtefactHandler instances
     *
     * @return An array of classes
     */
//    public Class[] getAllArtefacts() {
//        return allArtefactClassesArray;
//    }

    private Class[] populateAllClasses() {
        this.allClasses = loadedClasses.toArray(new Class[loadedClasses.size()]);
        return allClasses;
    }

    /**
     * Configures the loaded classes within the GriffonApplication instance using the registered ArtefactHandler instances
     *
     * @param classes The classes to configure
     */
//    private void configureLoadedClasses(Class[] classes) {
//        initArtefactHandlers();
//
//
//        artefactInfo.clear();
//        allArtefactClasses.clear();
//        allArtefactClassesArray = null;
//        this.allClasses = classes;
//
//        suspectArtefactInit = true;
//
//        try {
//
//            // first load the domain classes
//            log.debug("Going to inspect artefact classes.");
//            for (int i = 0; i < classes.length; i++) {
//                final Class theClass = classes[i];
//                log.debug("Inspecting [" + theClass.getName() + "]");
//                if (Modifier.isAbstract(theClass.getModifiers())) {
//                    log.debug("[" + theClass.getName() + "] is abstract.");
//                    continue;
//                }
//                if (allArtefactClasses.contains(theClass))
//                    continue;
//
//                // check what kind of artefact it is and add to corrent data structure
//                for (int j = 0; j < artefactHandlers.length; j++) {
//                    if (artefactHandlers[j].isArtefact(theClass)) {
//                        log.debug("Adding artefact " + theClass + " of kind " + artefactHandlers[j].getType());
//                        GriffonClass gclass = addArtefact(artefactHandlers[j].getType(), theClass);
//                        // Also maintain set of all artefacts (!= all classes loaded)
//                        allArtefactClasses.add(theClass);
//
//                        // Update per-artefact cache
//                        DefaultArtefactInfo info = getArtefactInfo(artefactHandlers[j].getType(), true);
//                        info.addGriffonClass(gclass);
//                        break;
//                    }
//                }
//            }
//
//            refreshArtefactGriffonClassCaches();
//
//        } finally {
//            suspectArtefactInit = false;
//        }
//
//        allArtefactClassesArray = (Class[]) allArtefactClasses.toArray(new Class[allArtefactClasses.size()]);
//
//        // Tell all artefact handlers to init now we've worked out which classes are which artefacts
//        for (int j = 0; j < artefactHandlers.length; j++) {
//            initializeArtefacts(artefactHandlers[j]);
//        }
//    }

    /**
     * <p>Tell all our artefact info objects to update their internal state after we've added a bunch of classes</p>
     */
//    private void refreshArtefactGriffonClassCaches() {
//        for (Iterator it = artefactInfo.values().iterator(); it.hasNext();) {
//            DefaultArtefactInfo info = (DefaultArtefactInfo) it.next();
//            info.updateComplete();
//        }
//    }

//    private void addToLoaded(Class clazz) {
//        this.loadedClasses.add(clazz);
//        populateAllClasses();
//    }

//    public GriffonControllerClass getScaffoldingController(GriffonDomainClass domainClass) {
//        if (domainClass == null) {
//            return null;
//        }
//
//        ArtefactInfo info = getArtefactInfo(ControllerArtefactHandler.TYPE, true);
//        GriffonClass[] controllerClasses = info.getGriffonClasses();
//
//        for (int i = 0; i < controllerClasses.length; i++) {
//            GriffonControllerClass controllerClass = (GriffonControllerClass) controllerClasses[i];
//            if (controllerClass.isScaffolding()) {
//                Class scaffoldedClass = controllerClass.getScaffoldedClass();
//                if (scaffoldedClass != null) {
//                    if (domainClass.getClazz().getName().equals(scaffoldedClass.getName())) {
//                        return controllerClass;
//                    }
//                } else if (domainClass.getName().equals(controllerClass.getName())) {
//                    return controllerClass;
//                }
//            }
//        }
//        return null;
//    }

//    public GriffonResourceLoader getResourceLoader() {
//        return resourceLoader;
//    }

    public GroovyClassLoader getClassLoader() {
        return this.cl;
    }

    public ConfigObject getConfig() {
        ConfigObject c = null; //ConfigurationHolder.getConfig();
        //noinspection ConstantConditions
        if (c == null) {
            ConfigSlurper configSlurper = new ConfigSlurper(GriffonUtil.getEnvironment());
            Map<String, Object> binding = new HashMap<String, Object>();

            // configure config slurper binding
            binding.put(CONFIG_BINDING_USER_HOME, System.getProperty("user.home"));
            binding.put(CONFIG_BINDING_GRIFFON_HOME, System.getProperty("griffon.home"));
            binding.put(CONFIG_BINDING_APP_NAME, getMetadata().get("app.name"));
            binding.put(CONFIG_BINDING_APP_VERSION, getMetadata().get("app.version"));

            configSlurper.setBinding(binding);
            try {
                Class scriptClass = getClassLoader()
                        .loadClass(CONFIG_CLASS);

                c = configSlurper.parse(scriptClass);
//                ConfigurationHolder.setConfig(c);
            } catch (ClassNotFoundException e) {
                log.debug("Could not find config class [" + CONFIG_CLASS + "]. This is probably nothing to worry about, it is not required to have a config: " + e.getMessage());
                // ignore, it is ok not to have a configuration file
                c = new ConfigObject();
            }


            try {
                Class dataSourceClass = getClassLoader()
                        .loadClass(DATA_SOURCE_CLASS);
                c.merge(configSlurper.parse(dataSourceClass));
            } catch (ClassNotFoundException e) {
                log.debug("Cound not find data source class [" + DATA_SOURCE_CLASS + "]. This may be what you are expecting, but will result in Griffon loading with an in-memory database");
                // ignore
            }
        }
        if (c == null) c = new ConfigObject();
//        ConfigurationHelper.initConfig(c,null, getClassLoader());
//        ConfigurationHolder.setConfig(c);
        return c;
    }


    /**
     * Retrieves the number of artefacts registered for the given artefactType as defined by the ArtefactHandler
     *
     * @param artefactType The type of the artefact as defined by the ArtefactHandler
     * @return The number of registered artefacts
     */
//    private int getArtefactCount(String artefactType) {
//        ArtefactInfo info = getArtefactInfo(artefactType);
//        return info == null ? 0 : info.getClasses().length;
//    }

    /**
     * Retrieves all classes loaded by the GriffonApplication
     *
     * @return All classes loaded by the GriffonApplication
     */
    public Class[] getAllClasses() {
        return this.allClasses;
    }

    /* *
     * Sets the parent ApplicationContext for the GriffonContext
     *
     * @param applicationContext The ApplicationContext
     * @throws BeansException Thrown when an error occurs setting the ApplicationContext
     */
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.parentContext = applicationContext;
//    }

    /* *
     * Retrieves the parent ApplicationContext for this GriffonContext
     *
     * @return The parent ApplicationContext
     */
//    public ApplicationContext getParentContext() {
//        return this.parentContext;
//    }

    /**
     * Retrieves a class from the GriffonApplication for the given name
     *
     * @param className The class name
     * @return Either the java.lang.Class instance or null if it doesn't exist
     */
    public Class getClassForName(String className) {
        if (StringUtils.isBlank(className)) {
            return null;
        }
        for (Class c : allClasses) {
            if (c.getName().equals(className)) {
                return c;
            }
        }
        return null;
    }

    /* *
     * Refreshes constraints defined by the DomainClassArtefactHandler
     *
     * @todo Move this out of GriffonContext
     */
//    public void refreshConstraints() {
//        ArtefactInfo info = getArtefactInfo(DomainClassArtefactHandler.TYPE, true);
//        GriffonClass[] domainClasses = info.getGriffonClasses();
//        for (int i = 0; i < domainClasses.length; i++) {
//            ((GriffonDomainClass) domainClasses[i]).refreshConstraints();
//        }
//    }

    /* *
     * Refreshes this GriffonContext, rebuilding all of the artefact definitions as defined by the registered ArtefactHandler instances
     */
//    public void refresh() {
//        configureLoadedClasses(this.cl.getLoadedClasses());
//    }

//    public void rebuild() {
//        this.initialised = false;
//        this.loadedClasses.clear();
//        initArtefactHandlers();
//
//        if (GriffonUtil.isDevelopmentEnv()) {
//            try {
//                loadGriffonApplicationFromResources(this.resources);
//                initialise();
//            } catch (IOException e) {
//                throw new GriffonConfigurationException("I/O error rebuilding GriffonContext: " + e.getMessage(), e);
//            }
//        } else {
//            throw new IllegalStateException("Cannot rebuild GriffonContext when not in development mode!");
//        }
//
//    }

    /* *
     * Retrieves the Spring Resource that was used to load the given Class
     *
     * @param theClazz The class
     * @return Either a Spring Resource or null if no Resource was found for the given class
     */
//    public Resource getResourceForClass(Class theClazz) {
//        if (this.resourceLoader == null) {
//            return null;
//        }
//        return this.resourceLoader.getResourceForClass(theClazz);
//    }

    /* *
     * Returns true if the given class is an artefact identified by one of the registered ArtefactHandler instances.
     * Uses class name equality to handle class reloading
     *
     * @param theClazz The class to check
     * @return True if it is an artefact
     */
//    public boolean isArtefact(Class theClazz) {
//        String className = theClazz.getName();
//        for (Iterator i = allArtefactClasses.iterator(); i.hasNext();) {
//            Class artefactClass = (Class) i.next();
//            if (className.equals(artefactClass.getName())) {
//                return true;
//            }
//        }
//        return false;
//    }

    /* *
     * Returns true if the specified class is of the given artefact type as defined by the ArtefactHandler
     *
     * @param artefactType The type of the artefact
     * @param theClazz     The class
     * @return True if it is of the specified artefactType
     * @ see org.codehaus.groovy.griffon.commons.ArtefactHandler
     */
//    public boolean isArtefactOfType(String artefactType, Class theClazz) {
//        ArtefactHandler handler = (ArtefactHandler) artefactHandlersByName.get(artefactType);
//        if (handler == null)
//            throw new GriffonConfigurationException("Unable to locate arefact handler for specified type: " + artefactType);
//
//        return handler.isArtefact(theClazz);
//    }

    /* *
     * Returns true if the specified class name is of the given artefact type as defined by the ArtefactHandler
     *
     * @param artefactType The type of the artefact
     * @param className    The class name
     * @return True if it is of the specified artefactType
     * @ see org.codehaus.groovy.griffon.commons.ArtefactHandler
     */
//    public boolean isArtefactOfType(String artefactType, String className) {
//        return getArtefact(artefactType, className) != null;
//    }

    /* *
     * Retrieves an artefact for the given type and nam
     *
     * @param artefactType The artefact type as defined by a registered ArtefactHandler
     * @param name         The name of the class
     * @return A GriffonClass instance or null if none could be found for the given artefactType and name
     */
//    public GriffonClass getArtefact(String artefactType, String name) {
//        ArtefactInfo info = getArtefactInfo(artefactType);
//        return info == null ? null : info.getGriffonClass(name);
//    }

//    private GriffonClass getFirstArtefact(String artefactType) {
//        ArtefactInfo info = getArtefactInfo(artefactType);
//        // This will throw AIOB if we have none
//        return info == null ? null : info.getGriffonClasses()[0];
//    }

    /* *
     * Returns all of the GriffonClass instances for the given artefactType as defined by the ArtefactHandler
     *
     * @param artefactType The type of the artefact defined by the ArtefactHandler
     * @return An array of classes for the given artefact
     */
//    public GriffonClass[] getArtefacts(String artefactType) {
//        ArtefactInfo info = getArtefactInfo(artefactType, true);
//        return info.getGriffonClasses();
//    }

    // This is next call is equiv to getControllerByURI / getTagLibForTagName
//    public GriffonClass getArtefactForFeature(String artefactType, Object featureID) {
//        ArtefactHandler handler = (ArtefactHandler) artefactHandlersByName.get(artefactType);
//        return handler.getArtefactForFeature(featureID);
//    }

    /* *
     * Adds an artefact of the given type for the given Class.
     *
     * @param artefactType  The type of the artefact as defined by a ArtefactHandler instance
     * @param artefactClass A Class instance that matches the type defined by the ArtefactHandler
     * @return The GriffonClass if successful or null if it couldn't be added
     * @throws GriffonConfigurationException If the specified Class is not the same as the type defined by the ArtefactHandler
     * @ see org.codehaus.groovy.griffon.commons.ArtefactHandler
     */
//    public GriffonClass addArtefact(String artefactType, Class artefactClass) {
//        // @todo should we filter abstracts here?
//        if (Modifier.isAbstract(artefactClass.getModifiers())) {
//            return null;
//        }
//
//        ArtefactHandler handler = (ArtefactHandler) artefactHandlersByName.get(artefactType);
//        if (handler.isArtefact(artefactClass)) {
//            GriffonClass artefactGriffonClass = handler.newArtefactClass(artefactClass);
//
//            // Store the GriffonClass in cache
//            DefaultArtefactInfo info = getArtefactInfo(artefactType, true);
//            info.addGriffonClass(artefactGriffonClass);
//            info.updateComplete();
//
//            addToLoaded(artefactClass);
//
//            if (isInitialised())
//                initializeArtefacts(artefactType);
//
//
//            return artefactGriffonClass;
//        } else {
//            throw new GriffonConfigurationException("Cannot add " + artefactType + " class ["
//                    + artefactClass + "]. It is not a " + artefactType + "!");
//        }
//    }

    /* *
     * Adds an artefact of the given type for the given GriffonClass.
     *
     * @param artefactType        The type of the artefact as defined by a ArtefactHandler instance
     * @param artefactGriffonClass A GriffonClass instance that matches the type defined by the ArtefactHandler
     * @return The GriffonClass if successful or null if it couldn't be added
     * @throws GriffonConfigurationException If the specified GriffonClass is not the same as the type defined by the ArtefactHandler
     * @see org.codehaus.groovy.griffon.commons.ArtefactHandler
     */
//    public GriffonClass addArtefact(String artefactType, GriffonClass artefactGriffonClass) {
//        // @todo should we filter abstracts here?
//        if (Modifier.isAbstract(artefactGriffonClass.getClazz().getModifiers())) {
//            return null;
//        }
//
//        ArtefactHandler handler = (ArtefactHandler) artefactHandlersByName.get(artefactType);
//        if (handler.isArtefactGriffonClass(artefactGriffonClass)) {
//            // Store the GriffonClass in cache
//            DefaultArtefactInfo info = getArtefactInfo(artefactType, true);
//            info.addGriffonClass(artefactGriffonClass);
//            info.updateComplete();
//
//            initializeArtefacts(artefactType);
//
//            return artefactGriffonClass;
//        } else {
//            throw new GriffonConfigurationException("Cannot add " + artefactType + " class ["
//                    + artefactGriffonClass + "]. It is not a " + artefactType + "!");
//        }
//    }

    /* *
     * Registers a new ArtefactHandler that is responsible for identifying and managing an particular artefact type that is defined by
     * some convention
     *
     * @param handler The ArtefactHandler to regster
     */
//    public void registerArtefactHandler(ArtefactHandler handler) {
//        artefactHandlersByName.put(handler.getType(), handler);
//    }

//    public ArtefactHandler[] getArtefactHandlers() {
//        return artefactHandlers;
//    }

    /* *
     * <p>Re-initialize the artefacts of the specified type. This gives handlers a chance to update caches etc</p>
     *
     * @param artefactType The type of artefact to init
     */
//    private void initializeArtefacts(String artefactType) {
//        ArtefactHandler handler = (ArtefactHandler) artefactHandlersByName.get(artefactType);
//        initializeArtefacts(handler);
//    }

    /* *
     * <p>Re-initialize the artefacts of the specified type. This gives handlers a chance to update caches etc</p>
     *
     * @param handler The handler to register
     */
//    private void initializeArtefacts(ArtefactHandler handler) {
//        if (handler != null) {
//            ArtefactInfo info = getArtefactInfo(handler.getType());
//            // Only init those that have data
//            if (info != null) {
//                //System.out.println("Initialising artefacts of kind " + handler.getType() + " with registered artefacts" + info.getGriffonClassesByName());
//                handler.initialize(info);
//            }
//        }
//    }

    /* *
     * <p>Get or create the cache of classes for the specified artefact type</p>
     *
     * @param artefactType The name of an artefact type
     * @param create       Set to true if you want non-existent caches to be created
     * @return The cache of classes for the type, or null if no cache exists and create is false
     */
//    private DefaultArtefactInfo getArtefactInfo(String artefactType, boolean create) {
//        DefaultArtefactInfo cache = (DefaultArtefactInfo) artefactInfo.get(artefactType);
//        if ((cache == null) && create) {
//            cache = new DefaultArtefactInfo();
//            artefactInfo.put(artefactType, cache);
//            cache.updateComplete();
//        }
//        return cache;
//    }

    /* *
     * <p>Get the cache of classes for the specified artefact type</p>
     *
     * @param artefactType The name of an artefact type
     * @return The cache of classes for the type, or null if no cache exists
     */
//    public ArtefactInfo getArtefactInfo(String artefactType) {
//        return getArtefactInfo(artefactType, false);
//    }


    /* *
     * <p>Overrides method invocation to return dynamic artefact methods</p>
     * <p>We will support getXXXXClasses() and isXXXXClass(class)</p>
     *
     * @param methodName The name of the method
     * @param args       The arguments to the method
     * @return The return value of the method
     * @todo this is REALLY ugly
     * @todo Need to add matches for add<Artefact>Class(java.lang.Class) and add<Artefact>Class(GriffonClass)
     */
//    public Object invokeMethod(String methodName, Object args) {
//
//        Object[] argsv = (Object[]) args;
//
//        Matcher match = GETCLASS_PATTERN.matcher(methodName);
//        // look for getXXXXClass(y)
//        match.find();
//        if (match.matches()) {
//            if (argsv.length > 0) {
//                if ((argsv.length != 1) || !(argsv[0] instanceof String)) {
//                    throw new IllegalArgumentException("Dynamic method get<Artefact>Class(artefactName) requires a " +
//                            "single String parameter");
//                } else {
//                    return getArtefact(match.group(2), argsv[0].toString());
//                }
//            } else {
//                // It's a no-param getter
//                return super.invokeMethod(methodName, args);
//            }
//        } else {
//            // look for isXXXXClass(y)
//            match = ISCLASS_PATTERN.matcher(methodName);
//            // find match
//            match.find();
//            if (match.matches()) {
//                if ((argsv.length != 1) || !(argsv[0] instanceof Class)) {
//                    throw new IllegalArgumentException("Dynamic method is<Artefact>Class(artefactClass) requires a " +
//                            "single Class parameter");
//                } else {
//                    return Boolean.valueOf(isArtefactOfType(match.group(2), (Class) argsv[0]));
//                }
//            } else {
//                // look for getXXXXClasses
//                match = GETCLASSESMETH_PATTERN.matcher(methodName);
//                // find match
//                match.find();
//                if (match.matches()) {
//                    String artefactName = GriffonClassUtils.getClassNameRepresentation(match.group(2));
//                    if (artefactHandlersByName.containsKey(artefactName)) {
//                        return getArtefacts(match.group(2));
//                    } else {
//                        throw new IllegalArgumentException("Dynamic method get<Artefact>Classes() called for " +
//                                "unrecognized artefact: " + match.group(2));
//                    }
//                } else {
//                    return super.invokeMethod(methodName, args);
//                }
//            }
//        }
//    }

    /* *
     * <p>Override property access and hit on xxxxClasses to return class arrays of artefacts</p>
     *
     * @param propertyName The name of the property, if it ends in *Classes then match and invoke internal ArtefactHandler
     * @return All the artifacts or delegate to super.getProperty
     */
//    public Object getProperty(String propertyName) {
//        // look for getXXXXClasses
//        final Matcher match = GETCLASSESPROP_PATTERN.matcher(propertyName);
//        // find match
//        match.find();
//        if (match.matches()) {
//            String artefactName = GriffonClassUtils.getClassNameRepresentation(match.group(1));
//            if (artefactHandlersByName.containsKey(artefactName)) {
//                return getArtefacts(artefactName);
//            }
//        }
//        return super.getProperty(propertyName);
//    }

    public void initialise() {
        // get all the classes that were loaded
        if (log.isDebugEnabled()) {
            log.debug("loaded classes: [" + loadedClasses + "]");
        }
        //Class[] classes =
                populateAllClasses();
        //configureLoadedClasses(classes);
        this.initialised = true;
    }

    public boolean isInitialised() {
        return this.initialised;
    }

    public Map getMetadata() {
        return applicationMeta;
    }

//    public GriffonClass getArtefactByLogicalPropertyName(String type, String logicalName) {
//        ArtefactInfo info = getArtefactInfo(type);
//        return info == null ? null : info.getGriffonClassByLogicalPropertyName(logicalName);
//    }

//    public void addArtefact(Class artefact) {
//        for (int i = 0; i < artefactHandlers.length; i++) {
//            ArtefactHandler artefactHandler = artefactHandlers[i];
//            if (artefactHandler.isArtefact(artefact)) {
//                addArtefact(artefactHandler.getType(), artefact);
//            }
//        }
//    }

//    public boolean isWarDeployed() {
//        Map metadata = getMetadata();
//        if(metadata != null) {
//            Object val = metadata.get(META_GRIFFON_WAR_DEPLOYED);
//            if(val != null && val.equals("true")) {
//                return true;
//            }
//        }
//        return false;
//    }

//    public void setBeanClassLoader(ClassLoader classLoader) {
//        this.beanClassLoader = classLoader;
//    }
}
