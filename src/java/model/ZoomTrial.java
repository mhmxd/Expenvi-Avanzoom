package model;

import enums.TaskType;

public class ZoomTrial extends Trial {
    public TaskType type;
    public final int startNotch;
    public final int targetNotch;

    public ZoomTrial(TaskType type, int startNotch, int targetNotch) {
        super();

        this.type = type;
        this.startNotch = startNotch;
        this.targetNotch = targetNotch;
    }

    @Override
    public String toString() {
        return "ZoomTrial{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", blockNum=" + blockNum +
                ", trialNum=" + trialNum +
                " startNotch=" + startNotch +
                ", targetNotch=" + targetNotch +
                '}';
    }
}
