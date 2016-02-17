package com.brohnson.jett.squat;

/**
 * Created by brett on 2/14/16.
 */
public class Squat {
    public static int REQUIRED_DEPTH = 0;
    boolean completed;
    int depth;
    public Squat(Integer[] angles, Long[] times){
        int min = 90;
        for(int a =0;a<angles.length;a++)
            if(min>angles[a])min=angles[a];
        this.depth = 0;
        completed = depth<=REQUIRED_DEPTH;
    }
}
