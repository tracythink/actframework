package act.util;

import act.Act;
import act.Destroyable;
import org.osgl.logging.L;
import org.osgl.logging.Logger;
import org.osgl.util.C;

import java.util.List;

public abstract class DestroyableBase implements Destroyable {

    private boolean destroyed;

    private List<Destroyable> subResources = C.newList();

    @Override
    public synchronized final void destroy() {
        if (destroyed) {
            return;
        }
        destroyed = true;
        Destroyable.Util.destroyAll(subResources);
        releaseResources();
    }

    @Override
    public final boolean isDestroyed() {
        return destroyed;
    }

    protected void releaseResources() {}

    public synchronized void addSubResource(Destroyable object) {
        subResources.add(object);
    }

}
