package listener;

import moose.Memo;

import java.util.EventListener;

public interface MooseListener extends EventListener {
    void mooseClicked(Memo mem);

    void mooseScrolled(Memo mem);

    void mooseWheelMoved(Memo mem);

    void moosePanned(Memo mem);

    void mooseGrabbed(Memo mem);

    void mooseReleased(Memo mem);
}
