package model;

import enums.Direction;

import java.util.Collections;
import java.util.List;

public class ScrollBlock extends Block {

    public ScrollBlock(int blkNum,
                       List<Direction> directions, List<Integer> distances, List<Integer> indicSizes) {

        for (Direction direction : directions) {
            for (int distance : distances) {
                for (int indicSize : indicSizes) {
                    trials.add(new ScrollTrial(direction, distance, indicSize));
                }
            }
        }

        setNumsIds(blkNum);
        Collections.shuffle(trials);
    }
}
