package act.view.rythm;

import act.Act;
import act.ActComponent;
import act.app.App;
import act.conf.AppConfig;
import act.util.ActContext;
import act.view.Template;
import act.view.VarDef;
import act.view.View;
import org.osgl.util.C;
import org.osgl.util.S;
import org.rythmengine.Rythm;
import org.rythmengine.RythmEngine;
import org.rythmengine.extension.ISourceCodeEnhancer;
import org.rythmengine.resource.ClasspathResourceLoader;
import org.rythmengine.template.ITemplate;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.rythmengine.conf.RythmConfigurationKey.*;

/**
 * Implement a view with Rythm Template Engine
 */
@ActComponent
public class RythmView extends View {

    ConcurrentMap<App, RythmEngine> engines = new ConcurrentHashMap<App, RythmEngine>();

    @Override
    public String name() {
        return "rythm";
    }

    @Override
    protected Template loadTemplate(String resourcePath, ActContext context) {
        RythmEngine engine = getEngine(context.app());
        return RythmTemplate.find(engine, resourcePath, context.app());
    }

    private RythmEngine getEngine(App app) {
        RythmEngine engine = engines.get(app);
        if (null == engine) {
            engine = createEngine(app);
            RythmEngine engine0 = engines.putIfAbsent(app, engine);
            if (null != engine0) {
                engine.shutdown();
                engine = engine0;
            }
        }
        return engine;
    }

    private RythmEngine createEngine(App app) {
        AppConfig config = app.config();
        Properties p = new Properties();

        p.put(ENGINE_MODE.getKey(), Act.mode().isDev() ? Rythm.Mode.dev : Rythm.Mode.prod);
        p.put(ENGINE_PLUGIN_VERSION.getKey(), "0.0.1"); // TODO: implementing versioning
        p.put(ENGINE_CLASS_LOADER_PARENT_IMPL.getKey(), app.classLoader());

        Map map = config.rawConfiguration();
        for (Object k : map.keySet()) {
            String key = k.toString();
            if (key.startsWith("rythm.")) {
                p.put(key, map.get(key));
            }
        }

        String appRestricted = p.getProperty("rythm.sandbox.restricted_classes", "");
        appRestricted += ";act.*";
        p.put(SANDBOX_RESTRICTED_CLASS.getKey(), appRestricted);

        String templateHome = config.templateHome();
        if (S.blank(templateHome) || "default".equals(templateHome)) {
            templateHome = "/" + name();
        }

        p.put(HOME_TEMPLATE.getKey(), new File(app.layout().resource(app.base()), templateHome));

        p.put(CODEGEN_SOURCE_CODE_ENHANCER.getKey(), new ISourceCodeEnhancer() {
            @Override
            public List<String> imports() {
                return C.list();
            }

            @Override
            public String sourceCode() {
                return "";
            }

            @Override
            public Map<String, ?> getRenderArgDescriptions() {
                Map<String, String> map = C.newMap();
                for (VarDef var : Act.viewManager().implicitActionViewVariables()) {
                    map.put(var.name(), var.type());
                }
                return map;
            }

            @Override
            public void setRenderArgs(ITemplate iTemplate) {
                // no need to set render args here as
                // it's all done at TemplateBase#exposeImplicitVariables
            }
        });

        RythmEngine engine = new RythmEngine(p);
        engine.resourceManager().addResourceLoader(new ClasspathResourceLoader(engine, "rythm"));
        return engine;
    }

    @Override
    protected void reload(App app) {
        engines.remove(app);
        super.reload(app);
    }
}
