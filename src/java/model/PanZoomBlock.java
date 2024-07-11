package model;

import java.util.Collections;

public class PanZoomBlock extends Block {

    public PanZoomBlock(int blkNum) {
        // TODO Come up with the arguments and impl.
        for (int r = 1; r <= 8; r++) {
            trials.add(new PanZoomTrial(r, 500));
        }

        // Set blkNum and id
        for (int t = 0; t < trials.size(); t++) {
            trials.get(t).blockNum = blkNum;
            trials.get(t).id = trials.get(t).blockNum * 100 + t + 1;
        }

        // Shuffle
        Collections.shuffle(trials);

        // Set the number (needs to be after shuffling)
        for (int t = 0; t < trials.size(); t++) {
            trials.get(t).trialNum = t + 1;
        }
    }
}
