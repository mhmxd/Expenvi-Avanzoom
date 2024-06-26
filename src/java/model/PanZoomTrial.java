package model;

import enums.Task;

import java.util.ArrayList;
import java.util.List;

public class PanZoomTrial extends BaseTrial {
    public double initZoomLvl;
    public int roomNum;

    public static final List<List<Integer>> rooms = new ArrayList<>();

    static {
        rooms.add(new ArrayList<>(List.of(9177, 1635, 2000)));
        rooms.add(new ArrayList<>(List.of(16600, 6650, 1200)));
        rooms.add(new ArrayList<>(List.of(16000, 9500, 1200)));
        rooms.add(new ArrayList<>(List.of(15500, 15500, 3000)));
        rooms.add(new ArrayList<>(List.of(9300, 14700, 600)));
        rooms.add(new ArrayList<>(List.of(2800, 14000, 1200)));
        rooms.add(new ArrayList<>(List.of(1700, 9000, 2000)));
        rooms.add(new ArrayList<>(List.of(5800, 4900, 600)));
    }

    public PanZoomTrial(int roomInd, double initZoomLvl) {
        super(Task.PAN_ZOOM);

        this.initZoomLvl = initZoomLvl;
        this.roomNum = roomInd + 1;
    }
}
