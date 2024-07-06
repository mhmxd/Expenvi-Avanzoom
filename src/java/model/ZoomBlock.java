package model;

import enums.TaskType;

import java.util.List;
import java.util.Map;

public class ZoomBlock extends Block {

    /**
     * Start & end notches are chosen randomly
     * @param blkNum Block number
     * @param type Zoom in/out
     * @param distances In notches
     */
    public ZoomBlock(int blkNum,
                     TaskType type, int nReps, List<Integer> distances) {

        // Zoom-in
//        int START_LEVEL = 1; // Outer ring
//        for (int j = 0; j < repetition; j++) {
//            for (int dist : TARGET_DISTS) {
//                conLog.debug("Dist = {}", dist);
//                // Choose the target randomly (from 1 to total n levels - dist - tol)
//                final int noelMult = Utils.randMulInt(
//                        dist + TARGET_TOLERANCE,
//                        MAX_NOTCHES - TARGET_TOLERANCE,
//                        NOTCHES_IN_ELEMENT);
//                final int targetLevel = noelMult + NOTCHES_IN_ELEMENT / 2; // Always center of the next circle
//
//                trials.add(new ZoomTrial(TaskType.ZOOM_IN, targetLevel - dist,
//                        targetLevel));
//                conLog.debug("ZI: Noel = {} -> Target = {} -> Start = {}",
//                        noelMult, targetLevel, targetLevel - dist);
//            }
//        }

//        int START_LEVEL = PanZoomPanel.ZOOM_N_ELEMENTS / 2 + 1; // Central circle
//        for (int dist : TARGET_DISTS) {
//            for (int j = 0; j < repetition; j++) {
//                conLog.debug("Dist = {}", dist);
//                // Choose the target randomly (from 1 to total n levels - dist - tol)
//                final int noelMult = Utils.randMulInt(
//                        TARGET_TOLERANCE,
//                        MAX_NOTCHES - TARGET_TOLERANCE - dist,
//                        NOTCHES_IN_ELEMENT);
//                final int targetLevel = noelMult + NOTCHES_IN_ELEMENT / 2; // Always center of the next circle
//
//                trials.add(new ZoomTrial(TaskType.ZOOM_OUT, targetLevel + dist,
//                        targetLevel));
//                conLog.debug("ZO: Noel = {} -> Target = {} -> Start = {}",
//                        noelMult, targetLevel, targetLevel + dist);
//            }
//        }

        for (int r = 0; r < nReps; r++) {
            for (int dist : distances) {
                // TODO Generate random start/end and create ZoomTrials
            }
        }
    }
}
