package ca.pkay.rcloneexplorer.RemoteConfig;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

    public static final ArrayList<String> providers = new ArrayList<>(Arrays.asList("ALIAS", "ONEDRIVE", "AMAZON CLOUD DRIVE", "WEBDAV", "B2", "BOX", "FTP", "HTTP", "HUBIC", "PCLOUD", "SFTP", "YANDEX", "DROPBOX"));
    private int[] selected = {-1};
    private RadioButton lastSelected;
    private ProviderSelectedListener listener;

    public RemotesConfigList() {}

    public static RemotesConfigList newInstance() { return new RemotesConfigList(); }

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

    private void setSelected(RadioButton radioButton, String provider) {
        radioButton.setChecked(true);
        if (lastSelected != null) {
            lastSelected.setChecked(false);
        }
        lastSelected = radioButton;
        selected[0] = providers.indexOf(provider);
    }

    private void setClickListeners(View view) {
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
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
                setSelected(rb, "BOX");
            }
        });
        view.findViewById(R.id.rb_box).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_box);
                setSelected(rb, "BOX");
            }
        });

        view.findViewById(R.id.provider_b2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_b2);
                setSelected(rb, "B2");
            }
        });
        view.findViewById(R.id.rb_b2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_b2);
                setSelected(rb, "B2");
            }
        });

        view.findViewById(R.id.provider_dropbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_dropbox);
                setSelected(rb, "DROPBOX");
            }
        });
        view.findViewById(R.id.rb_dropbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_dropbox);
                setSelected(rb, "DROPBOX");
            }
        });

        view.findViewById(R.id.provider_amazon_cloud_drive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_amazon_cloud_drive);
                setSelected(rb, "AMAZON CLOUD DRIVE");
            }
        });
        view.findViewById(R.id.rb_amazon_cloud_drive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_amazon_cloud_drive);
                setSelected(rb, "AMAZON CLOUD DRIVE");
            }
        });

        view.findViewById(R.id.provider_ftp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_ftp);
                setSelected(rb, "FTP");
            }
        });
        view.findViewById(R.id.rb_ftp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_ftp);
                setSelected(rb, "FTP");
            }
        });

        view.findViewById(R.id.provider_http).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_http);
                setSelected(rb, "HTTP");
            }
        });
        view.findViewById(R.id.rb_http).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_http);
                setSelected(rb, "HTTP");
            }
        });

        view.findViewById(R.id.provider_hubic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_hubic);
                setSelected(rb, "HUBIC");
            }
        });
        view.findViewById(R.id.rb_hubic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_hubic);
                setSelected(rb, "HUBIC");
            }
        });

        view.findViewById(R.id.provider_pcloud).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_pcloud);
                setSelected(rb, "PCLOUD");
            }
        });
        view.findViewById(R.id.rb_pcloud).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_pcloud);
                setSelected(rb, "PCLOUD");
            }
        });

        view.findViewById(R.id.provider_sftp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_sftp);
                setSelected(rb, "SFTP");
            }
        });
        view.findViewById(R.id.rb_sftp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_sftp);
                setSelected(rb, "SFTP");
            }
        });

        view.findViewById(R.id.provider_yandex).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_yandex);
                setSelected(rb, "YANDEX");
            }
        });
        view.findViewById(R.id.rb_yandex).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_yandex);
                setSelected(rb, "YANDEX");
            }
        });

        view.findViewById(R.id.provider_webdav).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_webdav);
                setSelected(rb, "WEBDAV");
            }
        });
        view.findViewById(R.id.rb_webdav).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_webdav);
                setSelected(rb, "WEBDAV");
            }
        });

        view.findViewById(R.id.provider_onedrive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_onedrive);
                setSelected(rb, "ONEDRIVE");
            }
        });
        view.findViewById(R.id.rb_onedrive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_onedrive);
                setSelected(rb, "ONEDRIVE");
            }
        });

        view.findViewById(R.id.provider_alias).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_alias);
                setSelected(rb, "ALIAS");
            }
        });
        view.findViewById(R.id.rb_alias).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.rb_alias);
                setSelected(rb, "ALIAS");
            }
        });
    }
}
