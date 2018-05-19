package ca.pkay.rcloneexplorer.RemoteConfig;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import ca.pkay.rcloneexplorer.MainActivity;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import es.dmoral.toasty.Toasty;

public class AmazonCloudStorageConfig extends Fragment {

    private Context context;
    private Rclone rclone;
    private View authView;
    private View formView;
    private AsyncTask authTask;
    private TextInputLayout remoteNameInputLayout;
    private TextInputLayout clientIdInputLayout;
    private TextInputLayout clientSecretInputLayout;
    private EditText remoteName;
    private EditText clientId;
    private EditText clientSecret;
    private EditText authUrl;
    private EditText tokenUrl;

    public AmazonCloudStorageConfig() {}

    public static AmazonCloudStorageConfig newInstance() { return new AmazonCloudStorageConfig(); }

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
        authView = view.findViewById(R.id.auth_screen);
        formView = view.findViewById(R.id.form);
        setUpForm(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (authTask != null) {
            authTask.cancel(true);
        }
    }

    private void setUpForm(View view) {
        View formContent = view.findViewById(R.id.form_content);
        int padding = getResources().getDimensionPixelOffset(R.dimen.config_form_template);
        remoteNameInputLayout = view.findViewById(R.id.remote_name_layout);
        remoteNameInputLayout.setVisibility(View.VISIBLE);
        remoteName = view.findViewById(R.id.remote_name);

        View clientIdTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        clientIdTemplate.setPadding(0, 0, 0, padding);
        ((ViewGroup) formContent).addView(clientIdTemplate);
        clientIdInputLayout = clientIdTemplate.findViewById(R.id.text_input_layout);
        clientIdInputLayout.setVisibility(View.VISIBLE);
        clientIdInputLayout.setHint(getString(R.string.amazon_cloud_storage_client_id_hint));
        clientId = clientIdTemplate.findViewById(R.id.edit_text);

        View clientSecretTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        clientSecretTemplate.setPadding(0, 0, 0, padding);
        ((ViewGroup) formContent).addView(clientSecretTemplate);
        clientSecretInputLayout = clientSecretTemplate.findViewById(R.id.text_input_layout);
        clientSecretInputLayout.setVisibility(View.VISIBLE);
        clientSecretInputLayout.setHint(getString(R.string.amazon_cloud_storage_client_secret_hint));
        clientSecret = clientSecretTemplate.findViewById(R.id.edit_text);

        View authUrlTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        authUrlTemplate.setPadding(0, 0, 0, padding);
        ((ViewGroup) formContent).addView(authUrlTemplate);
        TextInputLayout authUrlInputLayout = authUrlTemplate.findViewById(R.id.text_input_layout);
        authUrlInputLayout.setVisibility(View.VISIBLE);
        authUrlInputLayout.setHint(getString(R.string.auth_server_url_hint));
        authUrl = view.findViewById(R.id.edit_text);
        authUrlTemplate.findViewById(R.id.helper_text).setVisibility(View.VISIBLE);

        View tokenUrlTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        tokenUrlTemplate.setPadding(0, 0, 0, padding);
        ((ViewGroup) formContent).addView(tokenUrlTemplate);
        TextInputLayout tokenUrlInputLayout = tokenUrlTemplate.findViewById(R.id.text_input_layout);
        tokenUrlInputLayout.setVisibility(View.VISIBLE);
        tokenUrlInputLayout.setHint(getString(R.string.token_server_url_hint));
        tokenUrl = tokenUrlInputLayout.findViewById(R.id.edit_text);
        tokenUrlTemplate.findViewById(R.id.helper_text).setVisibility(View.VISIBLE);

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

        view.findViewById(R.id.launch_browser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://127.0.0.1:53682/auth";
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(context, Uri.parse(url));
            }
        });

        view.findViewById(R.id.cancel_auth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (authTask != null) {
                    authTask.cancel(true);
                }
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
    }

    private void setUpRemote() {
        boolean error = false;
        String name = remoteName.getText().toString();
        String clientIdString = clientId.getText().toString();
        String clientSecretString = clientSecret.getText().toString();
        String authUrlString = authUrl.getText().toString();
        String tokenUrlString = tokenUrl.getText().toString();

        if (name.trim().isEmpty()) {
            remoteNameInputLayout.setErrorEnabled(true);
            remoteNameInputLayout.setError(getString(R.string.remote_name_cannot_be_empty));
            error = true;
        } else {
            remoteNameInputLayout.setErrorEnabled(false);
        }
        if (clientIdString.trim().isEmpty()) {
            clientIdInputLayout.setErrorEnabled(true);
            clientIdInputLayout.setError(getString(R.string.required_field));
            error = true;
        } else {
            remoteNameInputLayout.setErrorEnabled(false);
        }
        if (clientSecretString.trim().isEmpty()) {
            clientSecretInputLayout.setErrorEnabled(true);
            clientSecretInputLayout.setError(getString(R.string.required_field));
            error = true;
        } else {
            clientSecretInputLayout.setErrorEnabled(false);
        }
        if (error) {
            return;
        }

        ArrayList<String> options = new ArrayList<>();
        options.add(name);
        options.add("amazon cloud drive");
        options.add("client_id");
        options.add(clientIdString);
        options.add("client_secret");
        options.add(clientSecretString);
        options.add("env_auth");
        options.add("true");

        if (!authUrlString.trim().isEmpty()) {
            options.add("auth_url");
            options.add(authUrlString);
        }
        if (!tokenUrlString.trim().isEmpty()) {
            options.add("token_url");
            options.add(tokenUrlString);
        }

        authTask = new ConfigCreate(options).execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class ConfigCreate extends AsyncTask<Void, Void, Boolean> {

        private ArrayList<String> options;
        private Process process;

        ConfigCreate(ArrayList<String> options) {
            this.options = new ArrayList<>(options);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            authView.setVisibility(View.VISIBLE);
            formView.setVisibility(View.GONE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            process = rclone.configCreate(options);

            String url = "http://127.0.0.1:53682/auth";

            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(context, Uri.parse(url));

            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return process.exitValue() == 0;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (process != null) {
                process.destroy();
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (!success) {
                Toasty.error(context, getString(R.string.error_creating_remote), Toast.LENGTH_SHORT, true).show();
            } else {
                Toasty.success(context, getString(R.string.remote_creation_success), Toast.LENGTH_SHORT, true).show();
            }
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
