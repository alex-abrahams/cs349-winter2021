package ca.uwaterloo.cs349;

import android.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Vector;

public class SharedViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    public Vector<Vector<Pair<Float,Float>>> gestures;

    public SharedViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is shared model");
        gestures = new Vector<>();
    }

    public LiveData<String> getText() {
        return mText;
    }
}