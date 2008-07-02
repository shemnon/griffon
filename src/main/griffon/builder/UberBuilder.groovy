/*
 * Copyright 2007 the original author or authors.
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

/*
 * Created by IntelliJ IDEA.
 * User: Danno
 * Date: Nov 7, 2007
 * Time: 2:50:58 PM
 * To change this template use File | Settings | File Templates.
 */
package griffon.builder

import griffon.groovy.util.Factory
import griffon.groovy.util.FactoryBuilderSupport
import org.codehaus.groovy.runtime.InvokerHelper

class UberBuilder extends FactoryBuilderSupport {

    protected final Map builderLookup = new LinkedHashMap()
    protected final List<UberBuilderRegistration> builderRegistration = new LinkedList<UberBuilderRegistration>()
    protected final Map setters = [:]
    protected final Map getters = [:]

    public UberBuilder() {
        loadBuilderLookups()
    }

    public UberBuilder(Object[] builders) {
        this()
        builders.each {if (it) uberInit(null, it)}
    }

    protected Object loadBuilderLookups() {}

    public final uberInit(Object prefix, Map builders) {
        if (prefix) {
            throw new RuntimeException("Prefixed maps not supported")
        } else {
            builders.each {k, v -> uberInit(k, v)}
        }
    }

    public final uberInit(Object prefix, Object[] builders) {
        if (prefix) {
            throw new RuntimeException("Prefixed maps not supported")
        } else {
            builders.each {v -> uberInit(prefix, v)}
        }
    }

    public final uberInit(Object prefix, Object builderKey) {
        def builder = builderLookup[builderKey]
        // make sure we won't self-loop
        if (builder && (builder != builderKey)) {
            // if we get more than one, we have more than this base case, so look it up
            uberInit(prefix, builder)
        } else {
            throw new RuntimeException("Cannot uberinit indirectly via key '$builderKey'")
        }
    }

    protected uberInit(Object prefix, Class klass) {
        if (builderLookup.containsKey(klass)) {
            uberInit(prefix, builderLookup[klass])
        } else if (FactoryBuilderSupport.isAssignableFrom(klass)) {
            uberInit(prefix, klass.newInstance())
        } else {
            throw new RuntimeException("Cannot uberinit indirectly from class'${klass.name}'")
        }
    }

    protected uberInit(Object prefix, FactoryBuilderSupport fbs) {
        builderRegistration.add(new UberBuilderRegistration(prefix, fbs))
        getVariables().putAll(fbs.variables)
        fbs.variables.clear()
        for (Closure delegate in fbs.attributeDelegates) {
            delegate.delegate = fbs
            proxyBuilder.@attributeDelegates.add( delegate );
        }
        for (Closure delegate in fbs.preInstantiateDelegates) {
            delegate.delegate = fbs
            proxyBuilder.@preInstantiateDelegates.add( delegate );
        }
        for (Closure delegate in fbs.postInstantiateDelegates) {
            delegate.delegate = fbs
            proxyBuilder.@postInstantiateDelegates.add( delegate );
        }
        for (Closure delegate in fbs.postNodeCompletionDelegates) {
            delegate.delegate = fbs
            proxyBuilder.@postNodeCompletionDelegates.add( delegate );
        }

        fbs.setProxyBuilder(this)
    }

    protected uberInit(Object prefix, Factory factory) {
        builderRegistration.add(new UberBuilderRegistration(prefix, factory))
    }


    Factory resolveFactory(Object name, Map attributes, Object value) {
        for (UberBuilderRegistration ubr in builderRegistration) {
            Factory factory = ubr.nominateFactory(name)
            if (factory) {
                if (ubr.builder) {
                    getProxyBuilder().getContext().put( CHILD_BUILDER, ubr.builder)
                } else {
                    getProxyBuilder().getContext().put( CHILD_BUILDER, proxyBuilder)
                }

                return factory
            }
        }
    }

    protected void setClosureDelegate( Closure closure, Object node ) {
        closure.setDelegate( currentBuilder );
    }

    public Object build(Script script) {
        synchronized (script) {
            MetaClass scriptMetaClass = script.getMetaClass();
            script.setMetaClass(new UberInterceptorMetaClass(scriptMetaClass, this));
            script.setBinding(this);
            return script.run();
        }
    }

    public Object getProperty(String s) {
        if (getters[s]) {
            return getters[s]()
        } else {
            return super.getProperty(s)
        }
    }

    public void setProperty(String s, Object o) {
        if (setters[s]) {
            setters[s](o)
        } else {
            super.setProperty(s, o)
        }
    }


        ///////////////////////////////////////////////////////////////////////////
        // these appear to be Groovy 1.6beta2 bugs?  It breaks without it!
        ///////////////////////////////////////////////////////////////////////////
        public Object invokeMethod(String methodName) {
            return super.invokeMethod(methodName)
        }

    ///////////////////////////////////////////////////////////////////////////
    // these appear to be Groovy 1.6beta2 bugs?  It breaks without it!
    ///////////////////////////////////////////////////////////////////////////
        public Object invokeMethod(String methodName, Object args) {
            return super.invokeMethod(methodName, args)
        }
}

class UberBuilderRegistration {

    Factory factory
    FactoryBuilderSupport builder
    String prefixString

    public UberBuilderRegistration(String prefixString, FactoryBuilderSupport builder) {
        this.@prefixString = prefixString
        this.@builder = builder
    }

    public UberBuilderRegistration(String prefixString, Factory factory) {
        this.@prefixString = prefixString
        this.@factory = factory
    }

    Factory nominateFactory(String name) {
        if (builder) {
            // need to turn off proxy to get at class durring lookup
            def oldProxy = builder.proxyBuilder
            try {
                builder.proxyBuilder = builder
                String localName = name
                if (prefixString && name.startsWith(prefixString)) {
                    localName = name.substring(prefixString.length())
                }
                localName = builder.getName(localName)
                if (builder.factories.containsKey(localName)) {
                    return builder.factories[localName]
                }
            } finally {
                builder.proxyBuilder = oldProxy
            }
        }
        if (factory) {
            if (name == prefixString) {
                return factory
            }
        }
        return null
    }

    public String toString() {
        return "UberBuilderRegistration{ factory '$factory' builder '$builder' prefix '$prefixString'"
    }
}

class UberInterceptorMetaClass extends DelegatingMetaClass {

    UberBuilder factory;

    public UberInterceptorMetaClass(MetaClass delegate, UberBuilder factory) {
        super(delegate);
        this.factory = factory;
    }

    public Object invokeMethod(Object object, String methodName, Object arguments) {
        try {
            return delegate.invokeMethod(object, methodName, arguments);
        } catch (MissingMethodException mme) {
            if (mme.method != methodName) {
                throw mme
            }
            // attempt method resolution
            for (UberBuilderRegistration reg in factory.builderRegistration) {
                try {
                    def builder = reg.builder
                    if (!builder.getMetaClass().respondsTo(builder, methodName).isEmpty()) {
                        return InvokerHelper.invokeMethod(builder, methodName, arguments);
                    }
                } catch (MissingMethodException mme2) {
                    if (mme2.method != methodName) {
                        throw mme
                    }
                    // drop the exception, there will be many
                }
            }
            // dispatch to factories if it is not a literal method
            try {
                return factory.invokeMethod(methodName, arguments);
            } catch (MissingMethodException mme2) {
                if (mme2.method != methodName) {
                    throw mme
                }
                mme2.printStackTrace(System.out);
                // chain secondary exception
                Throwable root = mme;
                while (root.getCause() != null) {
                    root = root.getCause();
                }
                root.initCause(mme2);
                // throw original
                throw mme;
            }
        }
    }

    public Object invokeStaticMethod(Object object, String methodName, Object[] arguments) {
        try {
            return delegate.invokeMethod(object, methodName, arguments);
        } catch (MissingMethodException mme) {
            if (mme.method != methodName) {
                throw mme
            }

            // attempt method resolution
            for (UberBuilderRegistration reg in factory.builderRegistration) {
                try {
                    def builder = reg.builder
                    if (!builder.getMetaClass().respondsTo(builder, methodName).isEmpty()) {
                        return InvokerHelper.invokeMethod(builder, methodName, arguments);
                    }
                } catch (MissingMethodException mme2) {
                    if (mme2.method != methodName) {
                        throw mme
                    }

                    // drop the exception, there will be many
                }
            }
            // dispatch to factories if it is not a literal method
            try {
                return factory.invokeMethod(methodName, arguments);
            } catch (MissingMethodException mme2) {
                if (mme2.method != methodName) {
                    throw mme
                }

                // chain secondary exception
                Throwable root = mme;
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
            if (mme.method != methodName) {
                throw mme
            }
            // attempt method resolution
            for (UberBuilderRegistration reg in factory.builderRegistration) {
                try {
                    def builder = reg.builder
                    if (!builder.getMetaClass().respondsTo(builder, methodName).isEmpty()) {
                        return InvokerHelper.invokeMethod(builder, methodName, arguments);
                    }
                } catch (MissingMethodException mme2) {
                    if (mme2.method != methodName) {
                        throw mme
                    }
                    // drop the exception, there will be many
                }
            }
            // dispatch to factories if it is not a literal method
            try {
                return factory.invokeMethod(methodName, arguments);
            } catch (MissingMethodException mme2) {
                if (mme2.method != methodName) {
                    throw mme
                }
                // chain secondary exception
                Throwable root = mme;
                while (root.getCause() != null) {
                    root = root.getCause();
                }
                root.initCause(mme2);
                // throw original
                throw mme;
            }
        }
    }

    public Object getProperty(Object o, String s) {
        try {
            return factory.getProperty(s)
        } catch (MissingPropertyException mpe) {
            mpe.printStackTrace(System.out);
            return super.getProperty(o, s)
        }
    }

    public void setProperty(Object o, String s, Object o1) {
        try {
            factory.setProperty(s, o1)
        } catch (MissingPropertyException mpe) {
            mpe.printStackTrace(System.out);
            super.getProperty(o, s, o1)
        }
    }


}
