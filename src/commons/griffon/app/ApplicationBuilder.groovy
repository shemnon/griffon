package griffon.app

import griffon.util.IGriffonApplication

/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: Sep 4, 2008
 * Time: 10:22:22 AM
 * To change this template use File | Settings | File Templates.
 */
class ApplicationBuilder extends FactoryBuilderSupport {

    public ApplicationBuilder(boolean init = true) {
        super(init)
    }

    public void registerVisuals() {
        registerFactory 'application', new ApplicationFactory()
    }

}