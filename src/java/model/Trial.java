package model;

import tool.Constants.STR;

abstract public class Trial {
    public int id;
//    public final TaskType task;
    public int blockNum;
    public int trialNum;
//    public final int level;
    public boolean finished;
    public int retries;

    public Trial() {
        this.finished = false;
        this.retries = 0;
    }

    @Override
    public String toString() {
        return "Trial{" +
                "id=" + id +
//                ", task='" + task + '\'' +
                ", blockId=" + blockNum +
                ", trialNum=" + trialNum +
                ", finished=" + finished +
                ", retries=" + retries +
                '}';
    }

    public String toLogValues() {
        return id + STR.SP +
                blockNum + STR.SP +
                trialNum + STR.SP;
    }
}
