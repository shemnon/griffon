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
    protected final List<UberBuilderRegistration> builderRegistation = new LinkedList<UberBuilderRegistration>()

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
        builderRegistation.add(new UberBuilderRegistration(prefix, fbs))
    }

    protected uberInit(Object prefix, Factory factory) {
        builderRegistation.add(new UberBuilderRegistration(prefix, factory)) 
    }


    Factory resolveFactory(Object name, Map attributes, Object value) {
        for (UberBuilderRegistration ubr in builderRegistation) {
            Factory factory = ubr.nominateFactory(name)
            if (factory) {
                if (ubr.builder) {
                    proxyBuilder.getContext().put( CURRENT_BUILDER, ubr.builder)
                } else {
                    proxyBuilder.getContext().put( CURRENT_BUILDER, proxyBuilder)
                }

                if (proxybuilder.getParentContext() != null) {
                    proxyBuilder.getContext().put( PARENT_BUILDER, proxybuilder.getParentContext().get(CURRENT_BUILDER));
                }
                return fac
            }
        }
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
            String localName = name
            if (prefix && prefixString && !name.startsWith(prefix)) {
                localName = name.subString(prefixString.length())
            }
            localName = builder.getName(localName)
            if (builder.factories.containsKey(localName)) {
                return builder.factories(localName)
            }
        }
        if (factory) {
            if (name == prefix) {
                return factory
            }
        }
        return null
    }
}