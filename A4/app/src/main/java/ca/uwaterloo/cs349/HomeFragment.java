package ca.uwaterloo.cs349;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Pair;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.*;

import static java.lang.Math.*;

public class HomeFragment extends Fragment {

    private SharedViewModel mViewModel;
    public Pair<Float, Float> initialPoint;
    public Vector<Pair<Float, Float>> points;
    public Vector<Pair<Float, Float>> finalPoints;
    public Path gesture;
    public boolean makingGesture;
    
    private Vector<Gesture> recognizeGesture(Gesture toRec) {
        Vector<Pair<Gesture, Double>> gestureDistances = new Vector<>();
        for (Gesture g : mViewModel.gestures.getValue()) {
            double distance = toRec.calculateDistance(g);
            gestureDistances.add(new Pair(g, distance));
        }
        Vector<Gesture> topThree = new Vector<>();
        Vector<Integer> alreadyFound = new Vector<>();
        if (!gestureDistances.isEmpty()) {
            for (int i = 0; i < 3; i++) {
                double smallest = -1;
                Gesture smallestGesture = new Gesture(null, null);
                int smallestAt = -1;
                for (int g = 0; g < gestureDistances.size(); g++) {
                    Pair<Gesture, Double> ges = gestureDistances.elementAt(g);
                        if ((ges.second < smallest || smallest < 0) && !alreadyFound.contains(g)) {
                            smallest = ges.second;
                            smallestGesture = ges.first;
                            smallestAt = g;
                        }
                }
                if (smallest != -1) {
                    topThree.add(smallestGesture);
                    alreadyFound.add(smallestAt);
                }
            }
        }
        return topThree;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        final SurfaceView surfaceView = root.findViewById(R.id.surface_home);
        final PathView pathView = root.findViewById(R.id.path_home);
        surfaceView.setBackgroundColor(0Xffffffff);
        mViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (mViewModel.getGestures().getValue().isEmpty()) {
                    textView.setText("add gestures before i can recognize them");
                } else {
                    textView.setText("draw a gesture and i will recognize it");
                }
            }
        });
        View.OnTouchListener toucher = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                Canvas c = new Canvas(Bitmap.createBitmap(pathView.getWidth(), pathView.getHeight(), Bitmap.Config.ALPHA_8));
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!makingGesture) {
                            initialPoint = new Pair<>(ev.getX(), ev.getY());
                            points = new Vector<>();
                            points.add(initialPoint);
                            gesture = new Path();
                            makingGesture = true;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (makingGesture) {
                            points.add(new Pair<>(ev.getX(), ev.getY()));
                            gesture.lineTo(ev.getX(), ev.getY());
                            System.out.println(ev.getX() + ", " + ev.getY());
                            pathView.points = points;
                            pathView.draw(c);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (makingGesture) {
                            makingGesture = false;
                            int gestureSize = points.size();
                            double interval = ((double)gestureSize)/128;
                            System.out.println("gesture size: " + gestureSize + ", interval: " + interval);
                            Vector<Pair<Float, Float>> resampledPoints = new Vector<>();
                            float centroidX = 0;
                            float centroidY = 0;
                            int total = 0;
                            for (double i=0;i<gestureSize;i+=interval) {
                                Pair<Float,Float> toAdd = points.elementAt((int) floor(i));
                                total++;
                                resampledPoints.add(toAdd);
                                centroidX += toAdd.first;
                                centroidY += toAdd.second;
                            }
                            centroidX = centroidX/total;
                            centroidY = centroidY/total;
                            double initialAngle = atan2(initialPoint.second-centroidY,initialPoint.first-centroidX);
                            finalPoints = new Vector<>();
                            for (Pair<Float, Float> point : resampledPoints) {
                                double angle = atan2(point.second-centroidY,point.first-centroidX);
                                double newAngle = angle-initialAngle;
                                double radius = hypot(point.first-centroidX,point.second-centroidY);
                                float newX = (float)(cos(newAngle)*radius);
                                float newY = (float)(sin(newAngle)*radius);
                                finalPoints.add(new Pair<>(newX,newY));
                            }
                            pathView.points = points;
                            pathView.draw(c);
                            System.out.println("drew gesture with " + points.size() + " points");
                            if (!mViewModel.gestures.getValue().isEmpty()) {
                                Vector<Gesture> recognized = recognizeGesture(new Gesture(finalPoints, "test"));
                                String text = "Top matches:\n";
                                for (int i = 0; i < min(3, recognized.size()); i++) {
                                    text += String.format("%d: %s\n", i + 1, recognized.elementAt(i).name);
                                }
                                textView.setText(text);
                            }
                        }
                        break;
                }
                return true;
            }
        };
        surfaceView.setOnTouchListener(toucher);
        return root;
    }



}