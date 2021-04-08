package ca.uwaterloo.cs349;

import android.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Vector;

public class SharedViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    public MutableLiveData<Vector<Gesture>> gestures;

    public SharedViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is shared model");
        gestures = new MutableLiveData<>();
        gestures.setValue(new Vector<>());
    }

    public LiveData<String> getText() {
        return mText;
    }
    public LiveData<Vector<Gesture>> getGestures() {
        return gestures;
    }
}