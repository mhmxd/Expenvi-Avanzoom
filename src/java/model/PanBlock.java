package model;

import tool.Utils;

import java.util.Collections;

public class PanBlock extends Block {

    public PanBlock(int blkNum,
                    int numReps) {

        // For each repetition: randomly choose the rotation for the short curve. Next two will be +120 and +240
        for (int i = 0; i < numReps; i++) {
            int rotation = Utils.randInt(0, 360);

            trials.add(new PanTrial(1, rotation));
            trials.add(new PanTrial(2, (rotation + 120) % 360)); // Go over the next rotation
            trials.add(new PanTrial(3, (rotation + 240) % 360)); // Go over the next rotation
        }

        setNumsIds(blkNum);
        Collections.shuffle(trials);
    }
}
