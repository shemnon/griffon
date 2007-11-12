/**
 * Created by IntelliJ IDEA.
 * User: Danno
 * Date: Nov 7, 2007
 * Time: 2:50:58 PM
 * To change this template use File | Settings | File Templates.
 */
package griffon.builder

class UberBuilder extends FactoryBuilderSupport {

    protected final Map builderLookup = new LinkedHashMap()
    protected final List<UberBuilderRegistration> builderRegistration = new LinkedList<UberBuilderRegistration>()

    public UberBuilder(Object[] builders) {
        loadBuilderLookups()
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
        variables.putAll(fbs.variables)
        fbs.variables.clear()
        for (Closure delegate in fbs.attributeDelegates) {
            delegate.delegate = fbs
            addAttributeDelegate(delegate)
        }
        for (Closure delegate in fbs.preInstantiateDelegates) {
            delegate.delegate = fbs
            addPreInstantiateDelegate(delegate)
        }
        for (Closure delegate in fbs.postInstantiateDelegates) {
            delegate.delegate = fbs
            addPostInstantiateDelegate(delegate)
        }
        for (Closure delegate in fbs.postNodeCompletionDelegates) {
            delegate.delegate = fbs
            addPostNodeCompletionDelegate {}(delegate)
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
                    proxyBuilder.getContext().put( CHILD_BUILDER, ubr.builder)
                } else {
                    proxyBuilder.getContext().put( CHILD_BUILDER, proxyBuilder)
                }

                return factory
            }
        }
    }

    protected void setClosureDelegate( Closure closure, Object node ) {
        closure.setDelegate( currentBuilder );
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