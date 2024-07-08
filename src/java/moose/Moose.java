package moose;

import listener.MooseListener;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import javax.swing.event.EventListenerList;

import static tool.Constants.*;

@SuppressWarnings("unused")
public class Moose {
    private final TaggedLogger conLog = Logger.tag(getClass().getSimpleName());

    private final EventListenerList mooseListener;

    public Moose() {
        this.mooseListener = new EventListenerList();
    }

    public synchronized void addMooseListener(MooseListener l) {
        if (l == null) {
            return;
        }
        mooseListener.add(MooseListener.class, l);
        conLog.info("Listener added – {}", mooseListener.getListenerCount());

    }

    public synchronized void removeMooseListener(MooseListener l) {
        if (l == null) {
            return;
        }
        mooseListener.remove(MooseListener.class, l);
        conLog.info("Listener removed");
    }

    public synchronized void processMooseEvent(Memo mem) {
        conLog.info("Process: {}", mem.getAction());
        MooseListener[] listeners = mooseListener.getListeners(MooseListener.class);
        int nL = mooseListener.getListenerCount();
        conLog.info("N. listeners = {}", nL);
        if (listeners.length > 0) {
            switch (mem.getAction()) {
                case STR.CLICK -> {
                    for (MooseListener l : listeners) {
                        l.mooseClicked(mem);
                    }
                }
                case STR.SCROLL -> {
                    for (MooseListener l : listeners) {
                        l.mooseScrolled(mem);
                    }
                }
                case STR.GRAB -> {
                    conLog.info("Grabbed");
                    for (MooseListener l : listeners) {
                        l.mooseGrabbed(mem);
                    }
                }
                case STR.REL -> {
                    conLog.info("Released");
                    for (MooseListener l : listeners) {
                        l.mooseReleased(mem);
                    }
                }
                case STR.PAN -> {
                    for (MooseListener l : listeners) {
                        l.moosePanned(mem);
                    }
                }
                case STR.ZOOM -> {
                    switch (mem.getMode()) {
                        case STR.ZOOM -> {
                            for (MooseListener l : listeners) {
                                l.mooseWheelMoved(mem);
                            }
                        }
//                        case STR.ZOOM_START -> {
//                            for (MooseListener l : listeners) {
//                                l.mooseZoomStart(mem);
//                            }
//                        }
                    }
                }
            }
        }
    }
}
