package ca.uwaterloo.cs349;

import android.util.Pair;

import java.util.Vector;

public class Gesture {
    public Vector<Pair<Float,Float>> points;
    public String name;
    public int N;

    public Gesture(Vector<Pair<Float, Float>> points, String name) {
        this.points = points;
        this.name = name;
        if (points != null) {
            N = points.size();
        } else {
            N = 0;
        }
    }

    public double calculateDistance(Gesture other) {
        double total = 0;
        for (int k=0;k<N;k++){
            total += Math.sqrt( Math.pow(other.points.elementAt(k).first-this.points.elementAt(k).first, 2) +
                                Math.pow(other.points.elementAt(k).second-this.points.elementAt(k).second, 2));
        }
        return total/N;
    }

    @Override
    public String toString() {
        return name;
    }
}
