package model;

import enums.Direction;

import java.util.Collections;
import java.util.List;

public class ScrollBlock extends Block {

    public ScrollBlock(int blkNum,
                       Direction[] directions, int[] distances, int[] indicSizes) {

        for (Direction direction : directions) {
            for (int distance : distances) {
                for (int indicSize : indicSizes) {
                    trials.add(new ScrollTrial(direction, distance, indicSize));
                }
            }
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
