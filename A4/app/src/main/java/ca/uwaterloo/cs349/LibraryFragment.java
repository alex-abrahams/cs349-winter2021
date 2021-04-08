package ca.uwaterloo.cs349;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.Vector;

public class LibraryFragment extends Fragment {

    private SharedViewModel mViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        View root = inflater.inflate(R.layout.fragment_library, container, false);
        final TextView textView = root.findViewById(R.id.text_library);
        final ListView listView = root.findViewById(R.id.list_library);
        mViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText((mViewModel.getGestures().getValue().isEmpty()) ? "no gestures saved" : "this is the list of gestures");
            }
        });
        mViewModel.getGestures().observe(getViewLifecycleOwner(), new Observer<Vector<Gesture>>() {
            @Override
            public void onChanged(Vector<Gesture> gs) {
                final ArrayAdapter adapter = new ArrayAdapter<Gesture>(getContext(), R.layout.activity_listview, gs);
                listView.setAdapter(adapter);
            }
        });
        AdapterView.OnItemLongClickListener clicker = new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Gesture selected = mViewModel.getGestures().getValue().elementAt((int)id);
                DialogInterface.OnClickListener yesOrNo = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                String deletedMessage = selected.name + " was deleted";
                                mViewModel.gestures.getValue().remove((int)id);
                                final ArrayAdapter adapter = new ArrayAdapter<Gesture>(getContext(), R.layout.activity_listview, mViewModel.gestures.getValue());
                                listView.setAdapter(adapter);
                                Snackbar message = Snackbar.make(root, deletedMessage, BaseTransientBottomBar.LENGTH_SHORT);
                                message.show();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:

                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Do you want to delete gesture "+selected.name+"?").setPositiveButton("Yes", yesOrNo)
                        .setNegativeButton("No", yesOrNo).show();
                return false;
            }
        };
        listView.setOnItemLongClickListener(clicker);
        return root;
    }
}