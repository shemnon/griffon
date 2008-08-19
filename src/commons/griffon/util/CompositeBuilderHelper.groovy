package griffon.util

import griffon.builder.UberBuilder
import groovy.util.FactoryBuilderSupport

/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: Jul 29, 2008
 * Time: 5:58:16 PM
 * To change this template use File | Settings | File Templates.
 */
class CompositeBuilderHelper {

    public static FactoryBuilderSupport createBuilder(ConfigObject builderConfig, Map targets) {
        UberBuilder uberBuilder = new UberBuilder()

        for (prefix in builderConfig) {
            String prefixName = prefix.key
            if (prefixName == "root") prefixName = ""
            for (builderClassName in prefix.value) {
                Class builderClass = Class.forName(builderClassName.key) //FIXME get correct classloader
                FactoryBuilderSupport localBuilder = uberBuilder.uberInit(prefixName, builderClass)
                for (partialTarget in builderClassName.value) {
                    if (partialTarget == 'view') {
                        // this needs special handling, skip it for now
                    } else {
                        MetaClass mc = targets[partialTarget.key]?.getMetaClass()
                        if (!mc) continue
                        for (groupName in partialTarget.value) {
                            if (groupName == "*") {
                                //FIXME handle add-all
                            } else {
                                def factories = localBuilder.getFactories()
                                def methods = localBuilder.getExplicitMethods()
                                def properties = localBuilder.getExplicitProperties()
                                def groupItems = localBuilder.getRegistrationGroupItems(groupName)
                                if (!groupItems) {
                                    continue
                                }
                                for (itemName in groupItems) {
                                    def resolvedName = "${prefixName}${itemName}"
                                    if (methods.containsKey(itemName)) {
                                        mc."$resolvedName" = methods[itemName]
                                    } else if (properties.containsKey(itemName)) {
                                        //FIXME do add property
                                    } else if (factories.containsKey(itemName)) {
                                        mc."${resolvedName}" = {Object... args->uberBuilder."$resolvedName"(*args)}
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
        return uberBuilder
    }

}
