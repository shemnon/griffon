/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.util;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.DelegatingMetaClass;
import groovy.lang.GroovyClassLoader;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;
import groovy.lang.Script;

import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mix of BuilderSupport and SwingBuilder's factory support.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Andres Almiray <aalmiray@users.sourceforge.com>
 */
public abstract class FactoryBuilderSupport extends Binding {
    public static final String CURRENT_FACTORY = "_CURRENT_FACTORY_";
    public static final String PARENT_FACTORY = "_PARENT_FACTORY_";
    public static final String PARENT_NODE = "_PARENT_NODE_";
    public static final String CURRENT_NODE = "_CURRENT_NODE_";
    public static final String PARENT_CONTEXT = "_PARENT_CONTEXT_";
    public static final String OWNER = "owner";
    private static final Logger LOG = Logger.getLogger( FactoryBuilderSupport.class.getName() );

    /**
     * Throws an exception if value is null.
     *
     * @param value the node's value
     * @param name the node's name
     */
    public static void checkValueIsNull( Object value, Object name ) {
        if( value != null ){
            throw new RuntimeException( "'" + name + "' elements do not accept a value argument." );
        }
    }

    /**
     * Checks type of value against buidler type
     * @param value the node's value
     * @param name the node's name
     * @param type a Class that may be assignable to the value's class
     * @return true if type is assignalbe to the value's class, false if value
     * is null.
     */
    public static boolean checkValueIsType( Object value, Object name, Class type ) {
        if( value != null ){
            if( type.isAssignableFrom( value.getClass() ) ){
                return true;
            }else{
                throw new RuntimeException( "The value argument of '" + name + "' must be of type "
                        + type.getName() );
            }
        }else{
            return false;
        }
    }

    /**
     * Checks values against factory's type
     *
     * @param value the node's value
     * @param name the node's name
     * @param type a Class that may be assignable to the value's class
     * @return Returns true if type is assignale to the value's class, false if value is
     * null or a String.
     */
    public static boolean checkValueIsTypeNotString( Object value, Object name, Class type ) {
        if( value != null ){
            if( type.isAssignableFrom( value.getClass() ) ){
                return true;
            }else if( value instanceof String ){
                return false;
            }else{
                throw new RuntimeException( "The value argument of '" + name + "' must be of type "
                        + type.getName() + " or a String." );
            }
        }else{
            return false;
        }
    }

    private LinkedList<Map<String, Object>> contexts = new LinkedList<Map<String, Object>>();
    private LinkedList<Closure> attributeDelegates = new LinkedList<Closure>();
    private Map<String, Factory> factories = new HashMap<String, Factory>();
    private Closure nameMappingClosure;
    private FactoryBuilderSupport proxyBuilder;
    private LinkedList<Closure> preInstantiateDelegates = new LinkedList<Closure>();
    private LinkedList<Closure> postInstantiateDelegates = new LinkedList<Closure>();
    private LinkedList<Closure> postNodeCompletionDelegates = new LinkedList<Closure>();

    public FactoryBuilderSupport() {
        this.proxyBuilder = this;
    }

    public FactoryBuilderSupport( Closure nameMappingClosure ) {
        this.nameMappingClosure = nameMappingClosure;
        this.proxyBuilder = this;
    }

    /**
     * @return the factory map (Unmodifiable Map).
     */
    public Map getFactories() {
        return Collections.unmodifiableMap( proxyBuilder.factories );
    }

    /**
     * @return the context of the current node.
     */
    public Map<String, Object> getContext() {
        if( !proxyBuilder.contexts.isEmpty() ){
            return proxyBuilder.contexts.getFirst();
        }
        return null;
    }

    /**
     * @return the current node being built.
     */
    public Object getCurrent() {
        if( !proxyBuilder.contexts.isEmpty() ){
            Map context = proxyBuilder.contexts.getFirst();
            return context.get( CURRENT_NODE );
        }
        return null;
    }

    /**
     * @return the factory that built the current node.
     */
    public Factory getCurrentFactory() {
        if( !proxyBuilder.contexts.isEmpty() ){
            Map context = proxyBuilder.contexts.getFirst();
            return (Factory) context.get( CURRENT_FACTORY );
        }
        return null;
    }

    /**
     * @return the factory of the parent of the current node.
     */
    public Factory getParentFactory() {
        if( !proxyBuilder.contexts.isEmpty() ){
            Map context = proxyBuilder.contexts.getFirst();
            return (Factory) context.get( PARENT_FACTORY );
        }
        return null;
    }

    /**
     * @return the parent of the current node.
     */
    public Object getParentNode() {
        if( !proxyBuilder.contexts.isEmpty() ){
            Map context = proxyBuilder.contexts.getFirst();
            return context.get( PARENT_NODE );
        }
        return null;
    }

    /**
     * @return the context of the parent of the current node.
     */
    public Map getParentContext() {
        if( !proxyBuilder.contexts.isEmpty() ){
            Map context = proxyBuilder.contexts.getFirst();
            return (Map) context.get( PARENT_CONTEXT );
        }
        return null;
    }

    /**
     * Convenience method when no arguments are required
     *
     * @return the result of the call
     * @param methodName the name of the method to invoke
     */
    public Object invokeMethod( String methodName ) {
        return proxyBuilder.invokeMethod( methodName, null );
    }

    public Object invokeMethod( String methodName, Object args ) {
        Object name = proxyBuilder.getName( methodName );
        Object result;
        Object previousContext = proxyBuilder.getContext();
        try{
            result = proxyBuilder.doInvokeMethod( methodName, name, args );
        }catch( RuntimeException e ){
            // remove contexts created after we started
            if (proxyBuilder.contexts.contains(previousContext)) {
                while (proxyBuilder.getContext() != previousContext) {
                    proxyBuilder.popContext();
                }
            }
            throw e;
        }
        return result;
    }

    /**
     * Add an attribute delegate so it can intercept attributes being set.
     * Attribute delegates are fired in a FILO pattern, so that nested delegates
     * get first crack.
     *
     * @param attrDelegate the closure to be called
     * @return attrDelegate
     */
    public Closure addAttributeDelegate( Closure attrDelegate ) {
        proxyBuilder.attributeDelegates.addFirst( attrDelegate );
        return attrDelegate;
    }

    /**
     * Remove the most recently added instance of the attribute delegate.
     *
     * @param attrDelegate the instance of the closure to be removed
     */
    public void removeAttributeDelegate( Closure attrDelegate ) {
        proxyBuilder.attributeDelegates.remove( attrDelegate );
    }

    /**
     * Add a preInstantiate delegate so it can intercept nodes before they are
     * created. PreInstantiate delegates are fired in a FILO pattern, so that
     * nested delegates get first crack.
     *
     * @param delegate the closure to invoke
     * @return delegate
     */
    public Closure addPreInstantiateDelegate( Closure delegate ) {
        proxyBuilder.preInstantiateDelegates.addFirst( delegate );
        return delegate;
    }

    /**
     * Remove the most recently added instance of the preInstantiate delegate.
     *
     * @param delegate the closure to invoke
     */
    public void removePreInstantiateDelegate( Closure delegate ) {
        proxyBuilder.preInstantiateDelegates.remove( delegate );
    }

    /**
     * Add a postInstantiate delegate so it can intercept nodes after they are
     * created. PostInstantiate delegates are fired in a FILO pattern, so that
     * nested delegates get first crack.
     *
     * @param delegate the closure to invoke
     * @return delegate
     */
    public Closure addPostInstantiateDelegate( Closure delegate ) {
        proxyBuilder.postInstantiateDelegates.addFirst( delegate );
        return delegate;
    }

    /**
     * Remove the most recently added instance of the postInstantiate delegate.
     *
     * @param delegate the closure to invoke
     */
    public void removePostInstantiateDelegate( Closure delegate ) {
        proxyBuilder.postInstantiateDelegates.remove( delegate );
    }

    /**
     * Add a nodeCompletion delegate so it can intercept nodes after they done
     * with building. NodeCompletion delegates are fired in a FILO pattern, so
     * that nested delegates get first crack.
     *
     * @param delegate the closure to invoke
     * @return delegate
     */
    public Closure addPostNodeCompletionDelegate( Closure delegate ) {
        proxyBuilder.postNodeCompletionDelegates.addFirst( delegate );
        return delegate;
    }

    /**
     * Remove the most recently added instance of the nodeCompletion delegate.
     *
     * @param delegate the closure to be removed
     */
    public void removePostNodeCompletionDelegate( Closure delegate ) {
        proxyBuilder.postNodeCompletionDelegates.remove( delegate );
    }

    /**
     * Registers a factory for a JavaBean.<br>
     * The JavaBean clas should have a no-args constructor.
     *
     * @param theName name of the node
     * @param beanClass the factory to handle the name
     */
    public void registerBeanFactory( String theName, final Class beanClass ) {
        proxyBuilder.registerFactory( theName, new AbstractFactory(){
            public Object newInstance( FactoryBuilderSupport builder, Object name, Object value,
                    Map properties ) throws InstantiationException, IllegalAccessException {
                if( checkValueIsTypeNotString( value, name, beanClass ) ){
                    return value;
                }else{
                    return beanClass.newInstance();
                }
            }
        } );
    }

    /**
     * Registers a factory for a node name.
     *
     * @param name the name of the node
     * @param factory the factory to return the values
     */
    public void registerFactory( String name, Factory factory ) {
        proxyBuilder.factories.put( name, factory );
    }

    /**
     * This method is responsible for instanciating a node and configure its
     * properties.
     *
     * @param name the name of the node
     * @param attributes the attributes for the node
     * @param value the value arguments for the node
     * @return the object return from the factory
     */
    protected Object createNode( Object name, Map attributes, Object value ) {
        Object node;

        Factory factory = proxyBuilder.resolveFactory( name, attributes, value );
        if( factory == null ){
            LOG.log( Level.WARNING, "Could not find match for name '" + name + "'" );
            return null;
        }
        proxyBuilder.getContext().put( CURRENT_FACTORY, factory );
        proxyBuilder.preInstantiate( name, attributes, value );
        try{
            node = factory.newInstance( this, name, value, attributes );
            if( node == null ){
                LOG.log( Level.WARNING, "Factory for name '" + name + "' returned null" );
                return null;
            }

            if( LOG.isLoggable( Level.FINE ) ){
                LOG.fine( "For name: " + name + " created node: " + node );
            }
        }catch( Exception e ){
            throw new RuntimeException( "Failed to create component for '" + name + "' reason: "
                    + e, e );
        }
        proxyBuilder.postInstantiate( name, attributes, node );
        proxyBuilder.handleNodeAttributes( node, attributes );
        return node;
    }

    /**
     * This is a hook for subclasses to plugin a custom strategy for mapping
     * names to factories.
     *
     * @param name the name of the factory
     * @param attributes the attributes from the node
     * @param value value arguments from te node
     * @return the Factory associated with name.<br>
     */
    protected Factory resolveFactory( Object name, Map attributes, Object value ) {
        return proxyBuilder.factories.get( name );
    }

    /**
     * This method is the workhorse of the builder.
     *
     * @param methodName the name of the method being invoked
     * @param name the name of the node
     * @param args the arguemtns passed into the node
     * @return the object from the factory
     */
    private Object doInvokeMethod( String methodName, Object name, Object args ) {
        Object node;
        Closure closure = null;
        List list = InvokerHelper.asList( args );

        if( proxyBuilder.getContexts().isEmpty() ){
            // should be called on first build method only
            proxyBuilder.newContext();
        }
        switch( list.size() ){
            case 0:
                node = proxyBuilder.createNode( name, Collections.EMPTY_MAP, null );
                break;
            case 1: {
                Object object = list.get( 0 );
                if( object instanceof Map ){
                    node = proxyBuilder.createNode( name, (Map) object, null );
                }else if( object instanceof Closure ){
                    closure = (Closure) object;
                    node = proxyBuilder.createNode( name, Collections.EMPTY_MAP, null );
                }else{
                    node = proxyBuilder.createNode( name, Collections.EMPTY_MAP, object );
                }
            }
                break;
            case 2: {
                Object object1 = list.get( 0 );
                Object object2 = list.get( 1 );
                if( object1 instanceof Map ){
                    if( object2 instanceof Closure ){
                        closure = (Closure) object2;
                        node = proxyBuilder.createNode( name, (Map) object1, null );
                    }else{
                        node = proxyBuilder.createNode( name, (Map) object1, object2 );
                    }
                }else{
                    if( object2 instanceof Closure ){
                        closure = (Closure) object2;
                        node = proxyBuilder.createNode( name, Collections.EMPTY_MAP, object1 );
                    }else if( object2 instanceof Map ){
                        node = proxyBuilder.createNode( name, (Map) object2, object1 );
                    }else{
                        throw new MissingMethodException( name.toString(), getClass(),
                                list.toArray(), false );
                    }
                }
            }
                break;
            case 3: {
                Object arg0 = list.get( 0 );
                Object arg1 = list.get( 1 );
                Object arg2 = list.get( 2 );
                if( arg0 instanceof Map && arg2 instanceof Closure ){
                    closure = (Closure) arg2;
                    node = proxyBuilder.createNode( name, (Map) arg0, arg1 );
                }else if( arg1 instanceof Map && arg2 instanceof Closure ){
                    closure = (Closure) arg2;
                    node = proxyBuilder.createNode( name, (Map) arg1, arg0 );
                }else{
                    throw new MissingMethodException( name.toString(), getClass(), list.toArray(),
                            false );
                }
            }
                break;
            default: {
                throw new MissingMethodException( name.toString(), getClass(), list.toArray(),
                        false );
            }

        }

        if( node == null ){
            if( proxyBuilder.getContexts().size() == 1 ){
                // pop the first context
                proxyBuilder.popContext();
            }
            return node;
        }

        Object current = proxyBuilder.getCurrent();
        if( current != null ){
            proxyBuilder.setParent( current, node );
        }

        if( closure != null ){
            if( proxyBuilder.getCurrentFactory().isLeaf() ){
                throw new RuntimeException( "'" + name + "' doesn't support nesting." );
            }
            // push new node on stack
            Object parentFactory = proxyBuilder.getCurrentFactory();
            Map parentContext = proxyBuilder.getContext();
            proxyBuilder.newContext();
            proxyBuilder.getContext().put( OWNER, closure.getOwner() );
            proxyBuilder.getContext().put( CURRENT_NODE, node );
            proxyBuilder.getContext().put( PARENT_FACTORY, parentFactory );
            proxyBuilder.getContext().put( PARENT_NODE, current );
            proxyBuilder.getContext().put( PARENT_CONTEXT, parentContext );
            // lets register the builder as the delegate
            proxyBuilder.setClosureDelegate( closure, node );
            closure.call();
            proxyBuilder.popContext();
        }

        proxyBuilder.nodeCompleted( current, node );
        node = proxyBuilder.postNodeCompletion( current, node );
        if( proxyBuilder.getContexts()
                .size() == 1 ){
            // pop the first context
            proxyBuilder.popContext();
        }
        return node;
    }

    /**
     * A hook to allow names to be converted into some other object such as a
     * QName in XML or ObjectName in JMX.
     *
     * @param methodName the name of the desired method
     * @return the object representing the name
     */
    protected Object getName( String methodName ) {
        if( proxyBuilder.nameMappingClosure != null ){
            return proxyBuilder.nameMappingClosure.call( methodName );
        }
        return methodName;
    }

    /**
     * Proxy builders are useful for changing the building context, thus
     * enabling mix &amp; match builders.
     * @return the current builder that serves as a proxy.<br>
     */
    protected FactoryBuilderSupport getProxyBuilder() {
        return proxyBuilder;
    }

    /**
     * Assigns any existing properties to the node.<br>
     * It will call attributeDelegates before passing control to the factory
     * that built the node.
     *
     * @param node the object returned by tne node factory
     * @param attributes the attributes for the node
     */
    protected void handleNodeAttributes( Object node, Map attributes ) {
        // first, short circuit
        if( node == null ){
            return;
        }

        for( Iterator iter = proxyBuilder.attributeDelegates.iterator(); iter.hasNext(); ){
            ((Closure) iter.next()).call( new Object[] { this, node, attributes } );
        }

        if( proxyBuilder.getCurrentFactory().onHandleNodeAttributes( this, node, attributes ) ){
            proxyBuilder.setNodeAttributes( node, attributes );
        }
    }

    /**
     * Pushes a new context on the stack.
     */
    protected void newContext() {
        proxyBuilder.contexts.addFirst( new HashMap<String, Object>() );
    }

    /**
     * A hook to allow nodes to be processed once they have had all of their
     * children applied.
     *
     * @param node the current node being processed
     * @param parent the parent of the node being processed
     */
    protected void nodeCompleted( Object parent, Object node ) {
        proxyBuilder.getCurrentFactory().onNodeCompleted( this, parent, node );
    }

    /**
     * Removes the last context from the stack.
     * @return the contet just removed
     */
    protected Map popContext() {
        if( !proxyBuilder.contexts.isEmpty() ){
            return proxyBuilder.contexts.removeFirst();
        }
        return null;
    }

    /**
     * A hook after the factory creates the node and before attributes are set.<br>
     * It will call any registered postInstantiateDelegates, if you override
     * this method be sure to call this impl somewhere in your code.
     *
     * @param name the name of the node
     * @param attributes the attributes for the node
     * @param node the object created by teh node factory
     */
    protected void postInstantiate( Object name, Map attributes, Object node ) {
        for( Iterator iter = proxyBuilder.postInstantiateDelegates.iterator(); iter.hasNext(); ){
            ((Closure) iter.next()).call( new Object[] { this, node, attributes } );
        }
    }

    /**
     * A hook to allow nodes to be processed once they have had all of their
     * children applied and allows the actual node object that represents the
     * Markup element to be changed.<br>
     * It will call any registered postNodeCompletionDelegates, if you override
     * this method be sure to call this impl at the end of your code.
     *
     * @param node the current node being processed
     * @param parent the parent of the node being processed
     * @return the node, possibly new, that represents the markup element
     */
    protected Object postNodeCompletion( Object parent, Object node ) {
        for( Iterator iter = proxyBuilder.postNodeCompletionDelegates.iterator(); iter.hasNext(); ){
            ((Closure) iter.next()).call( new Object[] { this, parent, node } );
        }

        return node;
    }

    /**
     * A hook before the factory creates the node.<br>
     * It will call any registered preInstantiateDelegates, if you override this
     * method be sure to call this impl somewhere in your code.
     *
     * @param name the name of the node
     * @param attributes the attributes of the node
     * @param value the value argument(s) of the node
     */
    protected void preInstantiate( Object name, Map attributes, Object value ) {
        for( Iterator iter = proxyBuilder.preInstantiateDelegates.iterator(); iter.hasNext(); ){
            ((Closure) iter.next()).call( new Object[] { this, value, attributes } );
        }
    }

    /**
     * Clears the context stack.
     */
    protected void reset() {
        proxyBuilder.contexts.clear();
    }

    /**
     * A strategy method to allow derived builders to use builder-trees and
     * switch in different kinds of builders. This method should call the
     * setDelegate() method on the closure which by default passes in this but
     * if node is-a builder we could pass that in instead (or do something wacky
     * too)
     *
     * @param closure the closure on which to call setDelegate()
     * @param node the node value that we've just created, which could be a
     *        builder
     */
    protected void setClosureDelegate( Closure closure, Object node ) {
        closure.setDelegate( this );
    }

    /**
     * Maps attributes key/values to properties on node.
     * @param node the object from the node
     * @param attributes the attributtes to be set
     */
    protected void setNodeAttributes( Object node, Map attributes ) {
        // set the properties
        for( Iterator iter = attributes.entrySet()
                .iterator(); iter.hasNext(); ){
            Map.Entry entry = (Map.Entry) iter.next();
            String property = entry.getKey().toString();
            Object value = entry.getValue();
            InvokerHelper.setProperty( node, property, value );
        }
    }

    /**
     * Strategy method to stablish parent/child relationships.
     * @param parent the object from the parent node
     * @param child the object from the child node
     */
    protected void setParent( Object parent, Object child ) {
        proxyBuilder.getCurrentFactory().setParent( this, parent, child );
        Factory parentFactory = proxyBuilder.getParentFactory();
        if( parentFactory != null ){
            parentFactory.setChild( this, parent, child );
        }
    }

    /**
     * Sets the builder to be used as a proxy.
     * @param proxyBuilder the new proxy
     */
    protected void setProxyBuilder( FactoryBuilderSupport proxyBuilder ) {
        this.proxyBuilder = proxyBuilder;
    }

    /**
     * @return the stack of available contexts.
     */
    protected LinkedList getContexts() {
        return proxyBuilder.contexts;
    }

    public Object build(Class viewClass) {
        if (Script.class.isAssignableFrom(viewClass)) {
            Script script = InvokerHelper.createScript(viewClass, this);
            return build(script);
        } else {
            throw new RuntimeException("Only scripts can be executed via build(Class)");
        }
    }

    public Object build(Script script) {
        synchronized (script) {
            MetaClass scriptMetaClass = script.getMetaClass();
            try {
                script.setMetaClass(new FactoryInterceptorMetaClass(scriptMetaClass, this));
                script.setBinding(this);
                return script.run();
            } finally {
                script.setMetaClass(scriptMetaClass);
            }
        }
    }

    public Object build(final String script, GroovyClassLoader loader) {
        return build(loader.parseClass(script));
    }

    /**
     * Switches the builder's proxyBuilder during the execution of a closure.<br>
     * This is useful to temporary change the building context to another builder
     * without the need for a contrived setup. It will also take care of restoring
     * the previous proxyBuilder when the execution finishes, even if an exception
     * was thrown from inside the closure.
     *
     * @param builder the temporary builder to switch to as proxyBuilder.
     * @param closure the closure to be executed under the temporary builder.
     *
     * @throws RuntimeException - any exception the closure might have thrown during
     * execution.
     * @return the execution result of the closure.
     */ 
    public Object withBuilder( FactoryBuilderSupport builder, Closure closure ) {
        if( builder == null || closure == null ) {
	    return null;
	}

	Object result = null;
        Object previousContext = proxyBuilder.getContext();
	FactoryBuilderSupport previousProxyBuilder = proxyBuilder;
	try {
            proxyBuilder = builder;
	    closure.setDelegate( builder );
	    result = closure.call();
	}
	catch( RuntimeException e ) {
            // remove contexts created after we started
            proxyBuilder = previousProxyBuilder;
            if (proxyBuilder.contexts.contains(previousContext)) {
                while (proxyBuilder.getContext() != previousContext) {
                    proxyBuilder.popContext();
                }
            }
            throw e;
	}
	finally {
            proxyBuilder = previousProxyBuilder;
	}

        return result;
    }

    /**
     * Switches the builder's proxyBuilder during the execution of a closure.<br>
     * This is useful to temporary change the building context to another builder
     * without the need for a contrived setup. It will also take care of restoring
     * the previous proxyBuilder when the execution finishes, even if an exception
     * was thrown from inside the closure. Additionally it will use the closure's
     * result as the value for the node identified by 'name'.
     *
     * @param builder the temporary builder to switch to as proxyBuilder.
     * @param name the node to build on the 'parent' builder.
     * @param closure the closure to be executed under the temporary builder.
     *
     * @throws RuntimeException - any exception the closure might have thrown during
     * execution.
     * @return a node that responds to value of name with the closure's result as its
     * value.
     */
    public Object withBuilder( FactoryBuilderSupport builder, String name, Closure closure ) {
       if( name == null ) {
          return null;
       }
       Object result = proxyBuilder.withBuilder( builder, closure );
       return proxyBuilder.invokeMethod( name, new Object[]{ result });
    }

    /**
     * Switches the builder's proxyBuilder during the execution of a closure.<br>
     * This is useful to temporary change the building context to another builder
     * without the need for a contrived setup. It will also take care of restoring
     * the previous proxyBuilder when the execution finishes, even if an exception
     * was thrown from inside the closure. Additionally it will use the closure's
     * result as the value for the node identified by 'name' and assign any attributes
     * that might have been set.
     *
     * @param attributes additional properties for the node on the parent builder.
     * @param builder the temporary builder to switch to as proxyBuilder.
     * @param name the node to build on the 'parent' builder.
     * @param closure the closure to be executed under the temporary builder.
     *
     * @throws RuntimeException - any exception the closure might have thrown during
     * execution.
     * @return a node that responds to value of name with the closure's result as its
     * value.
     */
    public Object withBuilder( Map attributes, FactoryBuilderSupport builder, String name, Closure closure ) {
       if( name == null ) {
          return null;
       }
       Object result = proxyBuilder.withBuilder( builder, closure );
       return proxyBuilder.invokeMethod( name, new Object[]{ attributes, result });
    }
}

class FactoryInterceptorMetaClass extends DelegatingMetaClass {

    FactoryBuilderSupport factory;

    public FactoryInterceptorMetaClass(MetaClass delegate, FactoryBuilderSupport factory) {
        super(delegate);
        this.factory = factory;
    }

    public Object invokeMethod(Object object, String methodName, Object arguments) {
        try {
            return delegate.invokeMethod(object, methodName, arguments);
        } catch (MissingMethodException mme) {
            // attempt factory resolution
            try {
                if (factory.getMetaClass().respondsTo(factory, methodName).isEmpty()) {
                    // dispatch to fectories if it is not a literal method
                    return factory.invokeMethod(methodName, arguments);
                } else {
                    return InvokerHelper.invokeMethod(factory, methodName, arguments);
                }
            } catch (MissingMethodException mme2) {
                // chain secondary exception
                Throwable root = mme.getCause();
                while (root.getCause() != null) {
                    root = root.getCause();
                }
                root.initCause(mme2);
                // throw original
                throw mme;
            }
        }
    }

    public Object invokeMethod(Object object, String methodName, Object[] arguments) {
        try {
            return delegate.invokeMethod(object, methodName, arguments);
        } catch (MissingMethodException mme) {
            // attempt factory resolution
            try {
                if (factory.getMetaClass().respondsTo(factory, methodName).isEmpty()) {
                    // dispatch to fectories if it is not a literal method
                    return factory.invokeMethod(methodName, arguments);
                } else {
                    return InvokerHelper.invokeMethod(factory, methodName, arguments);
                }
            } catch (MissingMethodException mme2) {
                // chain secondary exception
                Throwable root = mme.getCause();
                while (root.getCause() != null) {
                    root = root.getCause();
                }
                root.initCause(mme2);
                // throw original
                throw mme;
            }
        }
    }
}
