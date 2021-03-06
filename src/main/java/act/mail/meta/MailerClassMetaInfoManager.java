package act.mail.meta;

import act.app.App;
import act.asm.Type;
import act.util.DestroyableBase;
import org.osgl.util.C;

import java.util.Map;

import static act.Destroyable.Util.destroyAll;
import static act.app.App.logger;

public class MailerClassMetaInfoManager extends DestroyableBase {

    private Map<String, MailerClassMetaInfo> mailers = C.newMap();

    public MailerClassMetaInfoManager() {
    }

    @Override
    protected void releaseResources() {
        destroyAll(mailers.values());
        mailers.clear();
        super.releaseResources();
    }

    public void registerMailerMetaInfo(MailerClassMetaInfo metaInfo) {
        String className = Type.getObjectType(metaInfo.className()).getClassName();
        mailers.put(className, metaInfo);
        logger.trace("Mailer meta info registered for: %s", className);
    }

    public MailerClassMetaInfo mailerMetaInfo(String className) {
        return mailers.get(className);
    }

}
