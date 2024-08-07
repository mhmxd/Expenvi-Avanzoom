package model;

import enums.Task;

import java.net.URI;

import static tool.Resources.SVG;

public class PanTrial extends Trial {
    public int level;
    public URI uri;
    public Integer rotation;

    public PanTrial(int level, Integer rotation) {
        super();
        this.level = level;

        switch (level) {
            case 1 -> uri = SVG.PAN_LVL1_URI;
            case 2 -> uri = SVG.PAN_LVL2_URI;
            case 3 -> uri = SVG.PAN_LVL3_URI;
        }

        this.rotation = rotation;
    }

    @Override
    public String toString() {
        return "PanTrial{" +
                "id=" + id +
                ", level=" + level +
                ", rotation=" + rotation +
                '}';
    }
}
