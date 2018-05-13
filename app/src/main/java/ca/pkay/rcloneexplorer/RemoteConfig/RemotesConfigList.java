package ca.pkay.rcloneexplorer.RemoteConfig;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.Arrays;

import ca.pkay.rcloneexplorer.R;

public class RemotesConfigList extends Fragment {

    public interface ProviderSelectedListener {
        void onProviderSelected(int provider);
    }

    public static final ArrayList<String> providers = new ArrayList<>(Arrays.asList("DRIVE", "BOX"));
    private int[] selected = {-1};
    private ProviderSelectedListener listener;

    public RemotesConfigList() {}

    public static RemotesConfigList newInstance() { return new RemotesConfigList(); }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_config_list, container, false);
        setClickListeners(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ProviderSelectedListener) {
            listener = (ProviderSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ProviderSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private void setClickListeners(View view) {
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProviderSelected(-1);
            }
        });
        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProviderSelected(selected[0]);
            }
        });
        view.findViewById(R.id.provider_box).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_box);
                rb.setChecked(true);
                selected[0] = providers.indexOf("BOX");
            }
        });
    }
}
