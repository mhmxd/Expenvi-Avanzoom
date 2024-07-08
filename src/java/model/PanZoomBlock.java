package model;

import java.util.Collections;

public class PanZoomBlock extends Block {

    public PanZoomBlock(int blkNum) {
        // TODO Come up with the arguments and impl.
        for (int r = 1; r <= 8; r++) {
            trials.add(new PanZoomTrial(r, 500));
        }

        setNumsIds(blkNum);
        Collections.shuffle(trials);
    }
}
