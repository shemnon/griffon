package griffon.util;

import groovy.lang.Binding;
import groovy.util.ConfigObject;

import java.awt.Container;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: May 21, 2008
 * Time: 3:21:38 PM
 */
public interface IGriffonApplication {

    public ConfigObject getConfig();
    public void setConfig(ConfigObject config);

    public Binding getBindings();
    public void setBindings(Binding bindings);

    public Class getConfigClass();

    public Map<String, ?> getControllers();
    public Map<String, ?> getViews();

    public void attachRootPanel(Container rootPane);
    public void attachMenuBar(Container menuBar);

    public void initialize();
    public void startup();
    public void ready();
    public void shutdown();
}
