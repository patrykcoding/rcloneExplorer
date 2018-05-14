package ca.pkay.rcloneexplorer.RemoteConfig;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;

import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;

public class B2Config extends Fragment {

    private Rclone rclone;
    private TextInputLayout remoteNameInputLayout;
    private TextInputLayout accountInputLayout;
    private TextInputLayout keyInputLayout;
    private EditText remoteName;
    private EditText account;
    private EditText key;
    private EditText endpoint;

    public B2Config() {}

    public static B2Config newInstance() { return new B2Config(); }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() == null) {
            return;
        }
        rclone = new Rclone(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.remote_config_form, container, false);
        setUpForm(view);
        return view;
    }

    private void setUpForm(View view) {
        remoteNameInputLayout = view.findViewById(R.id.remote_name_layout);
        remoteNameInputLayout.setVisibility(View.VISIBLE);
        remoteName = view.findViewById(R.id.remote_name);

        accountInputLayout = view.findViewById(R.id.account_input_layout);
        accountInputLayout.setVisibility(View.VISIBLE);
        accountInputLayout.setHint(getString(R.string.account_id_hint));
        account = view.findViewById(R.id.account);

        keyInputLayout = view.findViewById(R.id.key_input_layout);
        keyInputLayout.setVisibility(View.VISIBLE);
        keyInputLayout.setHint(getString(R.string.application_key_hint));
        key = view.findViewById(R.id.key);

        TextInputLayout endpointInputLayout = view.findViewById(R.id.endpoint_input_layout);
        endpointInputLayout.setVisibility(View.VISIBLE);
        endpointInputLayout.setHint(getString(R.string.endpoint_hint));
        endpoint = view.findViewById(R.id.endpoint);

        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpRemote();
            }
        });

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
    }

    private void setUpRemote() {
        String name = remoteName.getText().toString();
        String accountString = account.getText().toString();
        String keyString = key.getText().toString();
        String endpointString = endpoint.getText().toString();
        boolean error = false;

        if (name.trim().isEmpty()) {
            remoteNameInputLayout.setErrorEnabled(true);
            remoteNameInputLayout.setError(getString(R.string.remote_name_cannot_be_empty));
            error = true;
        } else {
            remoteNameInputLayout.setErrorEnabled(false);
        }
        if (!accountString.trim().isEmpty()) {
            accountInputLayout.setErrorEnabled(true);
            accountInputLayout.setError(getString(R.string.required_field));
            error = true;
        } else {
            accountInputLayout.setErrorEnabled(false);
        }
        if (!keyString.trim().isEmpty()) {
            keyInputLayout.setErrorEnabled(true);
            keyInputLayout.setError(getString(R.string.required_field));
            error = true;
        } else {
            keyInputLayout.setErrorEnabled(false);
        }
        if (error) {
            return;
        }

        ArrayList<String> options = new ArrayList<>();
        options.add(name);
        options.add("b2");
        options.add("account");
        options.add(accountString);
        options.add("key");
        options.add(keyString);
        if (!endpointString.trim().isEmpty()) {
            options.add("endpoint");
            options.add(endpointString);
        }
        options.add("env_auth");
        options.add("true");

        Process process = rclone.configCreate(options);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
