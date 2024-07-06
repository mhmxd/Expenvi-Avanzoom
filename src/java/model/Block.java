package model;

import com.google.gson.Gson;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import tool.Utils;

import java.util.ArrayList;

public class Block {
    private final TaggedLogger conLog = Logger.tag(getClass().getSimpleName());

    public int blockNum;
    public final ArrayList<Trial> trials = new ArrayList<>();

    public void setNumsIds(int blkNum) {
        for (int t = 0; t < trials.size(); t++) {
            trials.get(t).blockNum = blkNum;
            trials.get(t).trialNum = t + 1;
            trials.get(t).id = trials.get(t).blockNum * 100 + trials.get(t).trialNum;
        }
    }

    /**
     * Create the Pan trials in this block
     * (uses constants from PanPanel)
     */
    public void createPanTrials() {
//        List<Trial> tempList = new ArrayList<>();
//
//        List<Trial> temp1 = new ArrayList<>();
//        List<Trial> temp2 = new ArrayList<>();
//        List<Trial> temp3 = new ArrayList<>();
//
//        Random rand = new SecureRandom();
//
//        int angle = 120; // Angle between the trials is always 120 (cause 3 levels)





//        int radius = 360 / PanPanel.NUM_PAN_TRIALS_IN_BLOCK;
//        for (int i = 0; i < PanPanel.NUM_PAN_TRIALS_IN_BLOCK; i++) {
//            int rotation = radius * i + rand.nextInt(radius);
//            temp1.add(new PanTrial(1, rotation));
//        }
//        Collections.shuffle(temp1);
//
//        radius = 360 / PanPanel.NUM_PAN_TRIALS_IN_BLOCK;
//        for (int i = 0; i < PanPanel.NUM_PAN_TRIALS_IN_BLOCK; i++) {
//            int rotation = radius * i + rand.nextInt(radius);
//            temp2.add(new PanTrial(2, rotation));
//        }
//        Collections.shuffle(temp2);
//
//        radius = 360 / PanPanel.NUM_PAN_TRIALS_IN_BLOCK;
//        for (int i = 0; i < PanPanel.NUM_PAN_TRIALS_IN_BLOCK; i++) {
//            int rotation = radius * i + rand.nextInt(radius);
//            temp3.add(new PanTrial(3, rotation));
//        }
//        Collections.shuffle(temp3);
//
//        List<Trial> temp = new ArrayList<>();
//        int sum = PanPanel.NUM_PAN_TRIALS_IN_BLOCK / 3;
//        for (int i = 0; i < 3; i++) {
//            temp.clear();
//            for (int j = 0; j < sum; j++) {
//                temp.add(temp1.remove(0));
//                temp.add(temp2.remove(0));
//                temp.add(temp3.remove(0));
//            }
//            Collections.shuffle(temp);
//
//            for (int j = 0; j < temp.size(); j++) {
//                Trial t = temp.get(j);
//                t.blockId = i + 1;
//                t.trialNum = j + 1;
//                t.id = t.blockId * 100 + t.trialNum;
//            }
//
//            trials.addAll(temp);
//        }
    }

    /**
     * Get a trial
     * @param trNum Trial number (starting from 1)
     * @return Trial
     */
    public Trial getTrial(int trNum) {
        if (trNum > trials.size()) return null;
        else return cloneTrial(trials.get(trNum - 1));
    }

    /**
     * Is the block finished?
     * @param trNum Trial number
     * @return True (trNum was the last number) or False
     */
    public boolean isBlockFinished(int trNum) {
        return trNum >= trials.size();
    }

    /**
     * Re-insert a trial into the rest
     * @param trNum Trial number
     */
    public void reInsertTrial(int trNum) {
        Trial trial = trials.get(trNum - 1);
        int randomIndex = Utils.randInt(trNum, trials.size());
        trials.add(randomIndex, cloneTrial(trial));

        // Refesh the nums
        for (int i = 0; i < trials.size(); i++) {
            trials.get(i).trialNum = i + 1;
        }
    }

    /**
     * Clone a trial
     * @param inTr Input trial
     * @return Clone trial
     */
    public Trial cloneTrial(Trial inTr) {
        final Gson gson = new Gson();
        final String trialJSON = gson.toJson(inTr);
        final Class<? extends Trial> trialType = inTr.getClass();

        return gson.fromJson(trialJSON, trialType);
    }

    @Override
    public String toString() {
        return "Block{" +
                "blockNum=" + blockNum +
                ", trials=" + trials +
                '}';
    }
}
