package ca.pkay.rcloneexplorer.Fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialOverlayLayout;
import com.leinardi.android.speeddial.SpeedDialView;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import ca.pkay.rcloneexplorer.BreadcrumbView;
import ca.pkay.rcloneexplorer.Dialogs.GoToDialog;
import ca.pkay.rcloneexplorer.Dialogs.InputDialog;
import ca.pkay.rcloneexplorer.Dialogs.LinkDialog;
import ca.pkay.rcloneexplorer.Dialogs.LoadingDialog;
import ca.pkay.rcloneexplorer.Dialogs.ServeDialog;
import ca.pkay.rcloneexplorer.Dialogs.SortDialog;
import ca.pkay.rcloneexplorer.FileComparators;
import ca.pkay.rcloneexplorer.Dialogs.FilePropertiesDialog;
import ca.pkay.rcloneexplorer.FilePicker;
import ca.pkay.rcloneexplorer.Items.DirectoryObject;
import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.Dialogs.OpenAsDialog;
import ca.pkay.rcloneexplorer.Items.SyncDirectionObject;
import ca.pkay.rcloneexplorer.MainActivity;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.RecyclerViewAdapters.FileExplorerRecyclerViewAdapter;
import ca.pkay.rcloneexplorer.Services.DeleteService;
import ca.pkay.rcloneexplorer.Services.DownloadService;
import ca.pkay.rcloneexplorer.Services.MoveService;
import ca.pkay.rcloneexplorer.Services.StreamingService;
import ca.pkay.rcloneexplorer.Services.SyncService;
import ca.pkay.rcloneexplorer.Services.ThumbnailsLoadingService;
import ca.pkay.rcloneexplorer.Services.UploadService;
import es.dmoral.toasty.Toasty;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class FileExplorerFragment extends Fragment implements   FileExplorerRecyclerViewAdapter.OnClickListener,
                                                                SwipeRefreshLayout.OnRefreshListener,
                                                                BreadcrumbView.OnClickListener,
                                                                OpenAsDialog.OnClickListener,
                                                                InputDialog.OnPositive,
                                                                GoToDialog.Callbacks,
                                                                SortDialog.OnClickListener,
                                                                ServeDialog.Callback {

    private static final String ARG_REMOTE = "remote_param";
    private static final String SHARED_PREFS_SORT_ORDER = "ca.pkay.rcexplorer.sort_order";
    private static final int FILE_PICKER_UPLOAD_RESULT = 186;
    private static final int FILE_PICKER_DOWNLOAD_RESULT = 204;
    private static final int FILE_PICKER_SYNC_RESULT = 45;
    private static final int STREAMING_INTENT_RESULT = 468;
    private final String SAVED_PATH = "ca.pkay.rcexplorer.FILE_EXPLORER_FRAG_SAVED_PATH";
    private final String SAVED_CONTENT = "ca.pkay.rcexplorer.FILE_EXPLORER_FRAG_SAVED_CONTENT";
    private final String SAVED_SEARCH_MODE = "ca.pkay.rcexplorer.FILE_EXPLORER_FRAG_SEARCH_MODE";
    private final String SAVED_SEARCH_STRING = "ca.pkay.rcexplorer.FILE_EXPLORER_FRAG_SEARCH_STRING";
    private final String SAVED_RENAME_ITEM = "ca.pkay.rcexplorer.FILE_EXPLORER_FRAG_RENAME_ITEM";
    private final String SAVED_SELECTED_ITEMS = "ca.pkay.rcexplorer.FILE_EXPLORER_FRAG_SELECTED_ITEMS";
    private final String SAVED_IS_IN_MOVE_MODE = "ca.pkay.rcexplorer.FILE_EXPLORER_FRAG_IS_IN_MOVE_MODE";
    private final String SAVED_START_AT_BOOT = "ca.pkay.rcexplorer.FILE_EXPLORER_FRAG_START_AT_BOOT";
    private final String SAVED_DOWNLOAD_LIST = "ca.pkay.rcexplorer.FILE_EXPLORER_FRAG_DOWNLOAD_LIST";
    private final String SAVED_MOVE_START_PATH = "ca.pkay.rcexplorer.FILE_EXPLORER_FRAG_MOVE_START_PATH";
    private final String SAVED_SYNC_DIRECTION = "ca.pkay.rcexplorer.FILE_EXPLORER_FRAG_SYNC_DIRECTION";
    private final String SAVED_SYNC_REMOTE_PATH = "ca.pkay.rcexplorer.FILE_EXPLORER_FRAG_SYNC_REMOTE_PATH";
    private String originalToolbarTitle;
    private Stack<String> pathStack;
    private Map<String, Integer> directoryPosition;
    private DirectoryObject directoryObject;
    private List<FileItem> moveList;
    private String moveStartPath;
    private List<FileItem> downloadList;
    private FileItem renameItem;
    private BreadcrumbView breadcrumbView;
    private Rclone rclone;
    private RemoteItem remote;
    private String remoteName;
    private FileExplorerRecyclerViewAdapter recyclerViewAdapter;
    private LinearLayoutManager recyclerViewLinearLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View searchBar;
    private AsyncTask fetchDirectoryTask;
    private boolean isRunning;
    private int sortOrder;
    private boolean isInMoveMode;
    private SpeedDialView fab;
    private MenuItem menuSelectAll;
    private MenuItem menuGoTo;
    private MenuItem menuLink;
    private MenuItem menuHttpServe;
    private MenuItem menuEmptyTrash;
    private Boolean isDarkTheme;
    private Boolean isSearchMode;
    private String searchString;
    private String syncRemotePath;
    private int syncDirection;
    private boolean is720dp;
    private boolean showThumbnails;
    private boolean isThumbnailsServiceRunning;
    private boolean startAtRoot;
    private boolean goToDefaultSet;
    private Context context;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FileExplorerFragment() {
    }

    @SuppressWarnings("unused")
    public static FileExplorerFragment newInstance(RemoteItem remoteItem) {
        FileExplorerFragment fragment = new FileExplorerFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_REMOTE, remoteItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            return;
        }
        remote = getArguments().getParcelable(ARG_REMOTE);
        if (remote == null) {
            return;
        }
        remoteName = remote.getName();
        pathStack = new Stack<>();
        directoryPosition = new HashMap<>();
        directoryObject = new DirectoryObject();

        String path;
        if (savedInstanceState == null) {
            path = "//" + remoteName;
            directoryObject.setPath(path);
        } else {
            path = savedInstanceState.getString(SAVED_PATH);
            if (path == null) {
                return;
            }
            directoryObject.setPath(path);
            directoryObject.setContent(savedInstanceState.<FileItem>getParcelableArrayList(SAVED_CONTENT));
            buildStackFromPath(remoteName, path);
            renameItem = savedInstanceState.getParcelable(SAVED_RENAME_ITEM);
        }

        if (getContext() == null) {
            return;
        }
        originalToolbarTitle = ((FragmentActivity) context).getTitle().toString();
        setTitle();
        setHasOptionsMenu(true);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sortOrder = sharedPreferences.getInt(SHARED_PREFS_SORT_ORDER, SortDialog.ALPHA_ASCENDING);
        showThumbnails = sharedPreferences.getBoolean(getString(R.string.pref_key_show_thumbnails), false);
        isDarkTheme = sharedPreferences.getBoolean(getString(R.string.pref_key_dark_theme), false);
        goToDefaultSet = sharedPreferences.getBoolean(getString(R.string.pref_key_go_to_default_set), false);

        if (goToDefaultSet) {
            startAtRoot = sharedPreferences.getBoolean(getString(R.string.pref_key_start_at_root), false);
        }

        rclone = new Rclone(getContext());

        isSearchMode = false;
        isInMoveMode = false;
        is720dp = getResources().getBoolean(R.bool.is720dp);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_explorer_list, container, false);
        if (savedInstanceState != null) {
            startAtRoot = savedInstanceState.getBoolean(SAVED_START_AT_BOOT);
        }

        if (showThumbnails) {
            startThumbnailService();
        }

        swipeRefreshLayout = view.findViewById(R.id.file_explorer_srl);
        swipeRefreshLayout.setOnRefreshListener(this);

        Context context = view.getContext();

        RecyclerView recyclerView = view.findViewById(R.id.file_explorer_list);
        recyclerViewLinearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setItemAnimator(new LandingAnimator());
        recyclerView.setLayoutManager(recyclerViewLinearLayoutManager);
        View emptyFolderView = view.findViewById(R.id.empty_folder_view);
        View noSearchResultsView = view.findViewById(R.id.no_search_results_view);
        recyclerViewAdapter = new FileExplorerRecyclerViewAdapter(context, emptyFolderView, noSearchResultsView, this);
        recyclerViewAdapter.showThumbnails(showThumbnails);
        recyclerView.setAdapter(recyclerViewAdapter);

        if (remote.isRemoteType(RemoteItem.SFTP) && !goToDefaultSet & savedInstanceState == null) {
            showSFTPgoToDialog();
        } else {
            if (directoryObject.isDirectoryContentEmpty()) {
                fetchDirectoryTask = new FetchDirectoryContent().execute();
                swipeRefreshLayout.setRefreshing(true);
            } else {
                recyclerViewAdapter.newData(directoryObject.getDirectoryContent());
            }
        }

        fab = view.findViewById(R.id.fab_fragment_file_explorer_list);
        fab.setOverlayLayout((SpeedDialOverlayLayout)view.findViewById(R.id.fab_overlay));
        fab.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                switch (actionItem.getId()) {
                    case R.id.fab_add_folder:
                        onCreateNewDirectory();
                        break;
                    case R.id.fab_upload:
                        onUploadFiles();
                        break;
                }
                return false;
            }
        });
        fab.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_upload, R.drawable.ic_file_upload)
                .setLabel(getString(R.string.fab_upload_files))
                .create());
        fab.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_add_folder, R.drawable.ic_create_new_folder)
                .setLabel(getString(R.string.fab_new_folder))
                .create());

        breadcrumbView = ((FragmentActivity) context).findViewById(R.id.breadcrumb_view);
        breadcrumbView.setOnClickListener(this);
        breadcrumbView.setVisibility(View.VISIBLE);
        breadcrumbView.addCrumb(remoteName, "//" + remoteName);
        if (savedInstanceState != null) {
            if (!directoryObject.getCurrentPath().equals("//" + remoteName)) {
                breadcrumbView.buildBreadCrumbsFromPath(directoryObject.getCurrentPath());
            }
        }

        searchBar = ((FragmentActivity) context).findViewById(R.id.search_bar);

        final TypedValue accentColorValue = new TypedValue ();
        context.getTheme ().resolveAttribute (R.attr.colorAccent, accentColorValue, true);
        view.findViewById(R.id.bottom_bar).setBackgroundColor(accentColorValue.data);
        view.findViewById(R.id.move_bar).setBackgroundColor(accentColorValue.data);
        if (view.findViewById(R.id.background) != null) {
            view.findViewById(R.id.background).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickOutsideOfView();
                }
            });
        }

        setBottomBarClickListeners(view);

        if (savedInstanceState != null && savedInstanceState.getBoolean(SAVED_SEARCH_MODE, false)) {
            searchString = savedInstanceState.getString(SAVED_SEARCH_STRING);
            searchClicked();
        }

        isRunning = true;
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceivers();

        if (showThumbnails) {
            startThumbnailService();
        }

        if (directoryObject.isContentValid()) {
            return;
        }
        if (fetchDirectoryTask != null) {
            fetchDirectoryTask.cancel(true);
        }
        swipeRefreshLayout.setRefreshing(true);
        fetchDirectoryTask = new FetchDirectoryContent(true).execute();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_PATH, directoryObject.getCurrentPath());
        ArrayList<FileItem> content = new ArrayList<>(directoryObject.getDirectoryContent());
        outState.putParcelableArrayList(SAVED_CONTENT, content);
        outState.putBoolean(SAVED_SEARCH_MODE, isSearchMode);
        outState.putParcelable(SAVED_RENAME_ITEM, renameItem);
        outState.putBoolean(SAVED_START_AT_BOOT, startAtRoot);
        outState.putInt(SAVED_SYNC_DIRECTION, syncDirection);
        if (isSearchMode) {
            outState.putString(SAVED_SEARCH_STRING, searchString);
        }
        if (recyclerViewAdapter.isInSelectMode()) {
            outState.putParcelableArrayList(SAVED_SELECTED_ITEMS, new ArrayList<>(recyclerViewAdapter.getSelectedItems()));
        }
        if (isInMoveMode) {
            outState.putBoolean(SAVED_IS_IN_MOVE_MODE, true);
            outState.putParcelableArrayList(SAVED_SELECTED_ITEMS, new ArrayList<>(moveList));
        }
        if (downloadList != null && !downloadList.isEmpty()) {
            outState.putParcelableArrayList(SAVED_DOWNLOAD_LIST, new ArrayList<>(downloadList));
        }
        if (moveStartPath != null) {
            outState.putString(SAVED_MOVE_START_PATH, moveStartPath);
        }
        if (syncRemotePath != null) {
            outState.putString(SAVED_SYNC_REMOTE_PATH, syncRemotePath);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }

        List<FileItem> selectedItems = savedInstanceState.getParcelableArrayList(SAVED_SELECTED_ITEMS);
        boolean moveMode = savedInstanceState.getBoolean(SAVED_IS_IN_MOVE_MODE, false);
        if (selectedItems != null && !selectedItems.isEmpty() && !moveMode) {
            recyclerViewAdapter.setSelectedItems(selectedItems);
            handleFilesSelected();
        }

        if (moveMode) {
            isInMoveMode = true;
            moveList = savedInstanceState.getParcelableArrayList(SAVED_SELECTED_ITEMS);
            recyclerViewAdapter.setMoveMode(true);
            ((FragmentActivity) context).setTitle(getString(R.string.select_destination));
            ((FragmentActivity) context).findViewById(R.id.move_bar).setVisibility(View.VISIBLE);
            fab.hide();
            fab.setVisibility(View.INVISIBLE);
            setFabBehaviour(false);
        }
        downloadList = savedInstanceState.getParcelableArrayList(SAVED_DOWNLOAD_LIST);
        moveStartPath = savedInstanceState.getString(SAVED_MOVE_START_PATH);
        syncDirection = savedInstanceState.getInt(SAVED_SYNC_DIRECTION, -1);
        syncRemotePath = savedInstanceState.getString(SAVED_SYNC_REMOTE_PATH);
    }

    private void setFabBehaviour(boolean enableSnackBarBehaviour) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();

        if (enableSnackBarBehaviour) {
            params.setBehavior(new SpeedDialView.ScrollingViewSnackbarBehavior());
            fab.requestLayout();
        } else {
            params.setBehavior(new SpeedDialView.NoBehavior());
            fab.requestLayout();
        }
    }

    private void setTitle() {
        String title;
        if (remote.isCrypt()) {
            title = "crypt" + " " + "(" + remote.getTypeReadable() + ")";
        } else {
            title = remote.getTypeReadable();
        }
        ((FragmentActivity) context).setTitle(title);
    }

    private void buildStackFromPath(String remote, String path) {
        String root = "//" + remote;
        if (root.equals(path)) {
            return;
        }
        pathStack.clear();
        pathStack.push(root);

        int index = 0;

        while ((index = path.indexOf("/", index)) > 0) {
            String p = path.substring(0, index);
            pathStack.push(p);
            index++;
        }
    }

    private void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.background_service_broadcast));
        LocalBroadcastManager.getInstance(context).registerReceiver(backgroundTaskBroadcastReceiver, intentFilter);
    }

    private BroadcastReceiver backgroundTaskBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String broadcastRemote = intent.getStringExtra(getString(R.string.background_service_broadcast_data_remote));
            String broadcastPath = intent.getStringExtra(getString(R.string.background_service_broadcast_data_path));
            String broadcastPath2 = intent.getStringExtra(getString(R.string.background_service_broadcast_data_path2));
            String path = directoryObject.getCurrentPath();
            if (!remoteName.equals(broadcastRemote)) {
                return;
            }

            if (path.equals(broadcastPath)) {
                if (fetchDirectoryTask != null) {
                    fetchDirectoryTask.cancel(true);
                }
                if (directoryObject.isPathInCache(broadcastPath)) {
                    directoryObject.removePathFromCache(broadcastPath);
                }
                fetchDirectoryTask = new FetchDirectoryContent(true).execute();
            } else if (directoryObject.isPathInCache(broadcastPath)) {
                directoryObject.removePathFromCache(broadcastPath);
            }

            if (broadcastPath2 == null) {
                return;
            }

            if (path.equals(broadcastPath2)) {
                if (fetchDirectoryTask != null) {
                    fetchDirectoryTask.cancel(true);
                }
                swipeRefreshLayout.setRefreshing(false);
                if (directoryObject.isPathInCache(broadcastPath2)) {
                    directoryObject.removePathFromCache(broadcastPath2);
                }
                fetchDirectoryTask = new FetchDirectoryContent(true).execute();
            } else if (directoryObject.isPathInCache(broadcastPath2)) {
                directoryObject.removePathFromCache(broadcastPath2);
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_UPLOAD_RESULT && resultCode == FragmentActivity.RESULT_OK) {
            @SuppressWarnings("unchecked")
            ArrayList<File> result = (ArrayList<File>) data.getSerializableExtra(FilePicker.FILE_PICKER_RESULT);
            ArrayList<String> uploadList = new ArrayList<>();
            for (File file : result) {
                uploadList.add(file.getPath());
            }

            for (String uploadFile : uploadList) {
                Intent intent = new Intent(getContext(), UploadService.class);
                intent.putExtra(UploadService.LOCAL_PATH_ARG, uploadFile);
                intent.putExtra(UploadService.UPLOAD_PATH_ARG, directoryObject.getCurrentPath());
                intent.putExtra(UploadService.REMOTE_ARG, remote);
                context.startService(intent);
            }
        } else if (requestCode == FILE_PICKER_DOWNLOAD_RESULT) {
            if (resultCode != FragmentActivity.RESULT_OK) {
                downloadList.clear();
                return;
            }
            String selectedPath = data.getStringExtra(FilePicker.FILE_PICKER_RESULT);
            recyclerViewAdapter.cancelSelection();

            for (FileItem downloadItem : downloadList) {
                Intent intent = new Intent(getContext(), DownloadService.class);
                intent.putExtra(DownloadService.DOWNLOAD_ITEM_ARG, downloadItem);
                intent.putExtra(DownloadService.DOWNLOAD_PATH_ARG, selectedPath);
                intent.putExtra(DownloadService.REMOTE_ARG, remote);
                context.startService(intent);
            }
            downloadList.clear();
        } else if (requestCode == FILE_PICKER_SYNC_RESULT && resultCode == FragmentActivity.RESULT_OK) {
            String path = data.getStringExtra(FilePicker.FILE_PICKER_RESULT);
            Intent intent = new Intent(getContext(), SyncService.class);
            intent.putExtra(SyncService.REMOTE_ARG, remote);
            intent.putExtra(SyncService.LOCAL_PATH_ARG, path);
            intent.putExtra(SyncService.SYNC_DIRECTION_ARG, syncDirection);
            intent.putExtra(SyncService.REMOTE_PATH_ARG, syncRemotePath);
            context.startService(intent);
        } else if (requestCode == STREAMING_INTENT_RESULT) {
            Intent serveIntent = new Intent(getContext(), StreamingService.class);
            context.stopService(serveIntent);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.file_explorer_folder_menu, menu);
        menuSelectAll = menu.findItem(R.id.action_select_all);
        menuGoTo = menu.findItem(R.id.action_go_to);
        menuLink = menu.findItem(R.id.action_link);
        menuHttpServe = menu.findItem(R.id.action_serve);
        menuEmptyTrash = menu.findItem(R.id.action_empty_trash);

        if (!remote.hasTrashCan()) {
            menu.findItem(R.id.action_empty_trash).setVisible(false);
        }
        if (remote.isCrypt()) {
            menu.findItem(R.id.action_link).setVisible(false);
        }
        if (!remote.isRemoteType(RemoteItem.SFTP)) {
            menu.findItem(R.id.action_go_to).setVisible(false);
        }
        menu.findItem(R.id.action_wrap_filenames).setChecked(true);

        if (isInMoveMode || recyclerViewAdapter.isInSelectMode()) {
            setOptionsMenuVisibility(false);
        }
    }

    private void setOptionsMenuVisibility(boolean setVisible) {
        if (menuSelectAll == null || menuGoTo == null || menuLink == null || menuHttpServe == null || menuEmptyTrash == null) {
            return;
        }

        menuHttpServe.setVisible(setVisible);
        if (!setVisible && isInMoveMode) {
            menuSelectAll.setVisible(false);
        } else {
            menuSelectAll.setVisible(true);
        }
        if (!remote.isCrypt()) {
            menuLink.setVisible(setVisible);
        }
        if (remote.hasTrashCan()) {
            menuEmptyTrash.setVisible(setVisible);
        }
        if (remote.isRemoteType(RemoteItem.SFTP)) {
            menuGoTo.setVisible(setVisible);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                searchClicked();
                return true;
            case R.id.action_sort:
                showSortMenu();
                return true;
            case R.id.action_select_all:
                recyclerViewAdapter.toggleSelectAll();
                return true;
            case R.id.action_serve:
                serve();
                return true;
            case R.id.action_empty_trash:
                emptyTrash();
                return true;
            case R.id.action_link:
                new LinkTask().execute(directoryObject.getCurrentPath());
            case R.id.action_wrap_filenames:
                wrapFilenames(item);
                return true;
            case R.id.action_sync:
                showSyncDialog(directoryObject.getCurrentPath());
                return true;
            case R.id.action_go_to:
                showSFTPgoToDialog();
                return true;
            case android.R.id.home:
                if (isInMoveMode) {
                    cancelMoveClicked();
                } else if (recyclerViewAdapter.isInSelectMode()) {
                    recyclerViewAdapter.cancelSelection();
                } else {
                    ((MainActivity)context).openNavigationDrawer();
                }
                return true;
            default:
                    return super.onOptionsItemSelected(item);
        }
    }

    private void serve() {
        ServeDialog serveDialog = new ServeDialog();
        serveDialog.setDarkTheme(isDarkTheme);
        serveDialog.show(getChildFragmentManager(), "serve dialog");
    }

    // serve callback
    @Override
    public void onServeOptionsSelected(int protocol, boolean allowRemoteAccess, String user, String password) {
        Intent intent = new Intent(getContext(), StreamingService.class);
        intent.putExtra(StreamingService.SERVE_PATH_ARG, directoryObject.getCurrentPath());
        intent.putExtra(StreamingService.REMOTE_ARG, remote);
        intent.putExtra(StreamingService.SHOW_NOTIFICATION_TEXT, true);
        intent.putExtra(StreamingService.ALLOW_REMOTE_ACCESS, allowRemoteAccess);
        intent.putExtra(StreamingService.AUTHENTICATION_USERNAME, user);
        intent.putExtra(StreamingService.AUTHENTICATION_PASSWORD, password);

        switch (protocol) {
            case Rclone.SERVE_PROTOCOL_HTTP: // HTTP
                intent.putExtra(StreamingService.SERVE_PROTOCOL, StreamingService.SERVE_HTTP);
                break;
            case Rclone.SERVE_PROTOCOL_FTP: // FTP
                intent.putExtra(StreamingService.SERVE_PROTOCOL, StreamingService.SERVE_FTP);
                break;
            case Rclone.SERVE_PROTOCOL_WEBDAV: // Webdav
                intent.putExtra(StreamingService.SERVE_PROTOCOL, StreamingService.SERVE_WEBDAV);
                break;
            default:
                return;
        }
        context.startService(intent);
    }

    private void emptyTrash() {
        AlertDialog.Builder builder;

        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }

        builder.setMessage(R.string.empty_trash_confirmation)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new EmptyTrashTask().execute();
                    }
                })
                .show();
    }

    private void showSFTPgoToDialog() {
        GoToDialog goToDialog = new GoToDialog()
                .isDarkTheme(isDarkTheme);
        goToDialog.show(getChildFragmentManager(), "go to dialog");
    }

    /*
     * Swipe to refresh
     */
    @Override
    public void onRefresh() {
        if (fetchDirectoryTask != null) {
            fetchDirectoryTask.cancel(true);
        }
        fetchDirectoryTask = new FetchDirectoryContent(true).execute();
    }

    private void wrapFilenames(MenuItem menuItem) {
        if (menuItem.isChecked()) {
            menuItem.setChecked(false);
            recyclerViewAdapter.setWrapFileNames(false);
        } else {
            menuItem.setChecked(true);
            recyclerViewAdapter.setWrapFileNames(true);
        }
    }

    private void startThumbnailService() {
        Intent serveIntent = new Intent(getContext(), ThumbnailsLoadingService.class);
        serveIntent.putExtra(ThumbnailsLoadingService.REMOTE_ARG, remote);
        context.startService(serveIntent);
        isThumbnailsServiceRunning = true;
    }

    private void searchClicked() {
        if (isSearchMode) {
            if (!is720dp) {
                breadcrumbView.setVisibility(View.VISIBLE);
            }
            searchBar.setVisibility(View.GONE);
            searchDirContent("");
            ((EditText)searchBar.findViewById(R.id.search_field)).setText("");
            recyclerViewAdapter.setSearchMode(false);
            isSearchMode = false;
        } else {
            if (!is720dp) {
                breadcrumbView.setVisibility(View.GONE);
            }
            searchBar.setVisibility(View.VISIBLE);
            recyclerViewAdapter.setSearchMode(true);
            isSearchMode = true;
        }
    }

    private void showOpenAsDialog(FileItem fileItem) {
        OpenAsDialog openAsDialog = new OpenAsDialog();
        openAsDialog
                .setFileItem(fileItem)
                .setDarkTheme(isDarkTheme);
        if (getFragmentManager() != null) {
            openAsDialog.show(getChildFragmentManager(), "open as");
        }
    }

    /*
     * Open As dialog callbacks
     */
    @Override
    public void onClickText(FileItem fileItem) {
        new DownloadAndOpen(DownloadAndOpen.OPEN_AS_TEXT).execute(fileItem);
    }

    @Override
    public void onClickAudio(FileItem fileItem) {
        new StreamTask(StreamTask.OPEN_AS_AUDIO).execute(fileItem);
    }

    @Override
    public void onClickVideo(FileItem fileItem) {
        new StreamTask(StreamTask.OPEN_AS_VIDEO).execute(fileItem);
    }

    @Override
    public void onClickImage(FileItem fileItem) {
        new DownloadAndOpen(DownloadAndOpen.OPEN_AS_IMAGE).execute(fileItem);
    }

    private void showFileProperties(FileItem fileItem) {
        FilePropertiesDialog filePropertiesDialog = new FilePropertiesDialog()
                .setFile(fileItem)
                .setRemote(remote)
                .setDarkTheme(isDarkTheme);
        if (remote.isCrypt()) {
            filePropertiesDialog.withHashCalculations(false);
        }
        if (getFragmentManager() != null) {
            filePropertiesDialog.show(getChildFragmentManager(), "file properties");
        }
    }

    private void setBottomBarClickListeners(final View view) {
        view.findViewById(R.id.file_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadList = new ArrayList<>(recyclerViewAdapter.getSelectedItems());
                downloadFiles();
            }
        });

        view.findViewById(R.id.file_move).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveFiles(recyclerViewAdapter.getSelectedItems());
            }
        });

        view.findViewById(R.id.file_rename).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renameItem = recyclerViewAdapter.getSelectedItems().get(0);
                renameFiles();
            }
        });

        view.findViewById(R.id.file_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFiles(recyclerViewAdapter.getSelectedItems());
            }
        });

        view.findViewById(R.id.cancel_move).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelMoveClicked();
            }
        });

        view.findViewById(R.id.select_move).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveLocationSelected();
            }
        });

        view.findViewById(R.id.new_folder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateNewDirectory();
            }
        });

        ((EditText)searchBar.findViewById(R.id.search_field)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchDirContent(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        searchBar.findViewById(R.id.search_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText searchField = searchBar.findViewById(R.id.search_field);
                if (searchField.getText().toString().isEmpty()) {
                    searchClicked();
                } else {
                    searchDirContent("");
                    searchField.setText("");
                }
            }
        });
    }

    private void searchDirContent(String search) {
        List<FileItem> content = directoryObject.getDirectoryContent();
        List<FileItem> currentShown = recyclerViewAdapter.getCurrentContent();
        List<FileItem> results = new ArrayList<>();

        searchString = search;

        if (search.isEmpty()) {
            if (currentShown.equals(content)) {
                return;
            } else {
                recyclerViewAdapter.newData(content);
            }
        }

        for (FileItem item : content) {
            String fileName = item.getName().toLowerCase();
            if (fileName.contains(search.toLowerCase())) {
                results.add(item);
            }
        }

        if (currentShown.equals(results)) {
            return;
        }
        recyclerViewAdapter.newData(results);
    }

    private void cancelMoveClicked() {
        setTitle();
        recyclerViewAdapter.setMoveMode(false);
        isInMoveMode = false;
        hideMoveBar();
        fab.show();
        fab.setVisibility(View.VISIBLE);
        setFabBehaviour(true);
        showNavDrawerButtonInToolbar();
        setOptionsMenuVisibility(true);
        recyclerViewAdapter.refreshData();

        if (moveStartPath != null && !moveStartPath.equals(directoryObject.getCurrentPath())) {
            if (fetchDirectoryTask != null) {
                fetchDirectoryTask.cancel(true);
            }
            if (directoryObject.isPathInCache(moveStartPath)) {
                directoryObject.restoreFromCache(moveStartPath);
                recyclerViewAdapter.newData(directoryObject.getDirectoryContent());
            } else {
                directoryObject.setPath(moveStartPath);
                recyclerViewAdapter.clear();
                fetchDirectoryTask = new FetchDirectoryContent(true).execute();
            }
            buildStackFromPath(remoteName, moveStartPath);
            breadcrumbView.clearCrumbs();
            if (!moveStartPath.equals("//" + remoteName)) {
                breadcrumbView.buildBreadCrumbsFromPath(directoryObject.getCurrentPath());
            }
            breadcrumbView.addCrumb(remoteName, "//" + remoteName);
            moveStartPath = null;
        }
    }

    private void moveLocationSelected() {
        setTitle();
        hideMoveBar();
        fab.show();
        fab.setVisibility(View.VISIBLE);
        setFabBehaviour(true);
        setOptionsMenuVisibility(true);
        recyclerViewAdapter.setMoveMode(false);
        recyclerViewAdapter.refreshData();
        isInMoveMode = false;
        showNavDrawerButtonInToolbar();
        String oldPath = moveList.get(0).getPath();
        int index = oldPath.lastIndexOf(moveList.get(0).getName());
        String path2;
        if (index > 0) {
            path2 = moveList.get(0).getPath().substring(0, index - 1);
        } else {
            path2 = "//" + remoteName;
        }
        for (FileItem moveItem : moveList) {
            Intent intent = new Intent(context, MoveService.class);
            intent.putExtra(MoveService.REMOTE_ARG, remote);
            intent.putExtra(MoveService.MOVE_DEST_PATH, directoryObject.getCurrentPath());
            intent.putExtra(MoveService.MOVE_ITEM, moveItem);
            intent.putExtra(MoveService.PATH, path2);
            context.startService(intent);
        }
        Toasty.info(context, getString(R.string.moving_info), Toast.LENGTH_SHORT, true).show();
        moveList.clear();
        moveStartPath = null;
    }

    private void showCancelButtonInToolbar() {
        ActionBar actionbar = ((AppCompatActivity) context).getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_cancel_white);
        }
    }

    private void showNavDrawerButtonInToolbar() {
        ActionBar actionbar = ((AppCompatActivity)context).getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
    }

    private void showSortMenu() {
        SortDialog sortDialog = new SortDialog();
        sortDialog
                .setTitle(R.string.sort)
                .setNegativeButton(R.string.cancel)
                .setPositiveButton(R.string.ok)
                .setSortOrder(sortOrder)
                .setDarkTheme(isDarkTheme);
        sortDialog.show(getChildFragmentManager(), "sort dialog");
    }

    /*
     * Sort Dialog callback
     */
    @Override
    public void onPositiveButtonClick(int sortById, int sortOrderId) {
        if (!directoryObject.isDirectoryContentEmpty()) {
            sortSelected(sortById, sortOrderId);
        }
    }

    private void sortSelected(int sortById, int sortOrderId) {
        List<FileItem> directoryContent = directoryObject.getDirectoryContent();

        switch (sortById) {
            case R.id.radio_sort_name:
                if (sortOrderId == R.id.radio_sort_ascending) {
                    Collections.sort(directoryContent, new FileComparators.SortAlphaAscending());
                    sortOrder = SortDialog.ALPHA_ASCENDING;
                } else {
                    Collections.sort(directoryContent, new FileComparators.SortAlphaDescending());
                    sortOrder = SortDialog.ALPHA_DESCENDING;
                }
                break;
            case R.id.radio_sort_date:
                if (sortOrderId == R.id.radio_sort_ascending) {
                    Collections.sort(directoryContent, new FileComparators.SortModTimeAscending());
                    sortOrder = SortDialog.MOD_TIME_ASCENDING;
                } else {
                    Collections.sort(directoryContent, new FileComparators.SortModTimeDescending());
                    sortOrder = SortDialog.MOD_TIME_DESCENDING;
                }
                break;
            case R.id.radio_sort_size:
                if (sortOrderId == R.id.radio_sort_ascending) {
                    Collections.sort(directoryContent, new FileComparators.SortSizeAscending());
                    sortOrder = SortDialog.SIZE_ASCENDING;
                } else {
                    Collections.sort(directoryContent, new FileComparators.SortSizeDescending());
                    sortOrder = SortDialog.SIZE_DESCENDING;
                }
                break;
        }
        directoryObject.setContent(directoryContent);

        if (isSearchMode) {
            List<FileItem> sortedSearch = new ArrayList<>();
            List<FileItem> searchResult = recyclerViewAdapter.getCurrentContent();
            for (FileItem item : directoryContent) {
                if (searchResult.contains(item)) {
                    sortedSearch.add(item);
                }
            }
            recyclerViewAdapter.updateSortedData(sortedSearch);
        } else {
            recyclerViewAdapter.updateSortedData(directoryContent);
        }
        if (sortOrder > 0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().putInt(SHARED_PREFS_SORT_ORDER, sortOrder).apply();
        }
    }

    private void sortDirectory() {
        List<FileItem> directoryContent = directoryObject.getDirectoryContent();
        switch (sortOrder) {
            case SortDialog.MOD_TIME_DESCENDING:
                Collections.sort(directoryContent, new FileComparators.SortModTimeDescending());
                sortOrder = SortDialog.MOD_TIME_ASCENDING;
                break;
            case SortDialog.MOD_TIME_ASCENDING:
                Collections.sort(directoryContent, new FileComparators.SortModTimeAscending());
                sortOrder = SortDialog.MOD_TIME_DESCENDING;
                break;
            case SortDialog.SIZE_DESCENDING:
                Collections.sort(directoryContent, new FileComparators.SortSizeDescending());
                sortOrder = SortDialog.SIZE_ASCENDING;
                break;
            case SortDialog.SIZE_ASCENDING:
                Collections.sort(directoryContent, new FileComparators.SortSizeAscending());
                sortOrder = SortDialog.SIZE_DESCENDING;
                break;
            case SortDialog.ALPHA_ASCENDING:
                Collections.sort(directoryContent, new FileComparators.SortAlphaAscending());
                sortOrder = SortDialog.ALPHA_ASCENDING;
                break;
            case SortDialog.ALPHA_DESCENDING:
            default:
                Collections.sort(directoryContent, new FileComparators.SortAlphaDescending());
                sortOrder = SortDialog.ALPHA_DESCENDING;
        }
        directoryObject.setContent(directoryContent);
    }

    private void onClickOutsideOfView() {
        if (recyclerViewAdapter.isInSelectMode()) {
            recyclerViewAdapter.cancelSelection();
        } else if (recyclerViewAdapter.isInMoveMode()) {
            cancelMoveClicked();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        isRunning = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isThumbnailsServiceRunning) {
            Intent intent = new Intent(context, ThumbnailsLoadingService.class);
            context.stopService(intent);
            isThumbnailsServiceRunning = false;
        }

        LocalBroadcastManager.getInstance(context).unregisterReceiver(backgroundTaskBroadcastReceiver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (fetchDirectoryTask != null) {
            fetchDirectoryTask.cancel(true);
        }
        breadcrumbView.clearCrumbs();
        breadcrumbView.setVisibility(View.GONE);
        searchBar.setVisibility(View.GONE);
        ((FragmentActivity) context).setTitle(originalToolbarTitle);
        showNavDrawerButtonInToolbar();
        isRunning = false;
        context = null;
    }

    public boolean onBackButtonPressed() {
        if (recyclerViewAdapter.isInSelectMode()) {
            recyclerViewAdapter.cancelSelection();
            showNavDrawerButtonInToolbar();
            return true;
        } else if (isSearchMode) {
            searchClicked();
            return true;
        } else if (fab.isOpen()) {
            fab.close(true);
            return true;
        } else if (pathStack.isEmpty()) {
            return false;
        }
        if (!isInMoveMode && !recyclerViewAdapter.isInSelectMode()) {
            fab.show();
        }
        if (fetchDirectoryTask != null) {
            fetchDirectoryTask.cancel(true);
        }
        swipeRefreshLayout.setRefreshing(false);
        if (fetchDirectoryTask != null) {
            fetchDirectoryTask.cancel(true);
        }
        breadcrumbView.removeLastCrumb();
        String path = pathStack.pop();
        recyclerViewAdapter.clear();
        if (!directoryObject.isContentValid(path)) {
            swipeRefreshLayout.setRefreshing(true);
            directoryObject.restoreFromCache(path);
            sortDirectory();
            recyclerViewAdapter.newData(directoryObject.getDirectoryContent());
            if (directoryPosition.containsKey(directoryObject.getCurrentPath())) {
                int position = directoryPosition.get(directoryObject.getCurrentPath());
                recyclerViewLinearLayoutManager.scrollToPositionWithOffset(position, 10);
            }
            fetchDirectoryTask = new FetchDirectoryContent(true).execute();
        } else if (directoryObject.isPathInCache(path)) {
            directoryObject.restoreFromCache(path);
            sortDirectory();
            recyclerViewAdapter.newData(directoryObject.getDirectoryContent());
            if (directoryPosition.containsKey(directoryObject.getCurrentPath())) {
                int position = directoryPosition.get(directoryObject.getCurrentPath());
                recyclerViewLinearLayoutManager.scrollToPositionWithOffset(position, 10);
            }
        } else {
            directoryObject.setPath(path);
            fetchDirectoryTask = new FetchDirectoryContent().execute();
        }
        return true;
    }

    @Override
    public void onFileClicked(FileItem fileItem) {
        String type = fileItem.getMimeType();
        if (type.startsWith("video/") || type.startsWith("audio/")) {
            // stream video or audio
            new StreamTask().execute(fileItem);
        } else {
            // download and open
            new DownloadAndOpen().execute(fileItem);
        }
    }

    @Override
    public void onDirectoryClicked(FileItem fileItem, int position) {
        directoryPosition.put(directoryObject.getCurrentPath(), position);
        breadcrumbView.addCrumb(fileItem.getName(), fileItem.getPath());
        swipeRefreshLayout.setRefreshing(true);
        pathStack.push(directoryObject.getCurrentPath());
        if (!isInMoveMode && !recyclerViewAdapter.isInSelectMode()) {
            fab.show();
        }

        if (isSearchMode) {
            searchClicked();
        }

        if (fetchDirectoryTask != null) {
            fetchDirectoryTask.cancel(true);
        }

        if (!directoryObject.isContentValid(fileItem.getPath())) {
            swipeRefreshLayout.setRefreshing(true);
            directoryObject.restoreFromCache(fileItem.getPath());
            sortDirectory();
            recyclerViewAdapter.newData(directoryObject.getDirectoryContent());
            fetchDirectoryTask = new FetchDirectoryContent(true).execute();
        } else if (directoryObject.isPathInCache(fileItem.getPath())) {
            directoryObject.restoreFromCache(fileItem.getPath());
            sortDirectory();
            recyclerViewAdapter.newData(directoryObject.getDirectoryContent());
            swipeRefreshLayout.setRefreshing(false);
        } else {
            directoryObject.setPath(fileItem.getPath());
            recyclerViewAdapter.clear();
            fetchDirectoryTask = new FetchDirectoryContent().execute();
        }
    }

    @Override
    public void onFilesSelected() {
        handleFilesSelected();
    }

    private void handleFilesSelected() {
        int numOfSelected = recyclerViewAdapter.getNumberOfSelectedItems();

        if (numOfSelected > 0) { // something is selected
            ((FragmentActivity) context).setTitle(numOfSelected + " " + getString(R.string.selected));
            showBottomBar();
            fab.hide();
            fab.setVisibility(View.INVISIBLE);
            setFabBehaviour(false);
            setOptionsMenuVisibility(false);
            showCancelButtonInToolbar();
            if (numOfSelected > 1) {
                ((FragmentActivity) context).findViewById(R.id.file_rename).setAlpha(.5f);
                ((FragmentActivity) context).findViewById(R.id.file_rename).setClickable(false);
            } else {
                ((FragmentActivity) context).findViewById(R.id.file_rename).setAlpha(1f);
                ((FragmentActivity) context).findViewById(R.id.file_rename).setClickable(true);
            }
        }
    }

    @Override
    public void onFileDeselected() {
        if (!isInMoveMode && !recyclerViewAdapter.isInSelectMode()) {
            setTitle();
            hideBottomBar();
            fab.show();
            fab.setVisibility(View.VISIBLE);
            setFabBehaviour(true);
            setOptionsMenuVisibility(true);
            showNavDrawerButtonInToolbar();
        } else {
            handleFilesSelected();
        }
    }

    @Override
    public void onFileOptionsClicked(View view, FileItem fileItem) {
        showFileMenu(view, fileItem);
    }

    private void showFileMenu(View view, final FileItem fileItem) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.file_explorer_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_file_properties:
                        showFileProperties(fileItem);
                        break;
                    case R.id.action_open_as:
                        showOpenAsDialog(fileItem);
                        break;
                    case R.id.action_serve:
                        String[] serveOptions = new String[] {"HTTP", "Webdav"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setItems(serveOptions, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getContext(), StreamingService.class);
                                switch (which) {
                                    case 0: // HTTP
                                        intent.putExtra(StreamingService.SERVE_PATH_ARG, fileItem.getPath());
                                        intent.putExtra(StreamingService.REMOTE_ARG, remote);
                                        intent.putExtra(StreamingService.SERVE_PROTOCOL, StreamingService.SERVE_HTTP);
                                        intent.putExtra(StreamingService.SHOW_NOTIFICATION_TEXT, true);
                                        break;
                                    case 1: // Webdav
                                        intent.putExtra(StreamingService.SERVE_PATH_ARG, fileItem.getPath());
                                        intent.putExtra(StreamingService.REMOTE_ARG, remote);
                                        intent.putExtra(StreamingService.SERVE_PROTOCOL, StreamingService.SERVE_WEBDAV);
                                        intent.putExtra(StreamingService.SHOW_NOTIFICATION_TEXT, true);
                                        break;
                                    default:
                                        return;
                                }
                                context.startService(intent);
                            }
                        });
                        builder.setTitle(R.string.pick_a_protocol);
                        builder.show();
                        break;
                    case R.id.action_download:
                        downloadList = new ArrayList<>();
                        downloadList.add(fileItem);
                        downloadFiles();
                        break;
                    case R.id.action_move:
                        moveFiles(Collections.singletonList(fileItem));
                        break;
                    case R.id.action_rename:
                        renameItem = fileItem;
                        renameFiles();
                        break;
                    case R.id.action_delete:
                        deleteFiles(Collections.singletonList(fileItem));
                        break;
                    case R.id.action_link:
                        new LinkTask().execute(fileItem.getPath());
                        break;
                    case R.id.action_sync:
                        showSyncDialog(fileItem.getPath());
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        popupMenu.show();
        if (fileItem.isDir()) {
            popupMenu.getMenu().findItem(R.id.action_open_as).setVisible(false);
        } else {
            popupMenu.getMenu().findItem(R.id.action_sync).setVisible(false);
        }
        if (remote.isCrypt()) {
            popupMenu.getMenu().findItem(R.id.action_link).setVisible(false);
        }
        if (remote.isCrypt()) {
            popupMenu.getMenu().findItem(R.id.action_link).setVisible(false);
        }
    }

    @Override
    public void onBreadCrumbClicked(String path) {
        if (fab.isOpen()) {
            fab.close(true);
        }
        if (isSearchMode) {
            searchClicked();
        }
        if (!isInMoveMode && !recyclerViewAdapter.isInSelectMode()) {
            fab.show();
        }
        if (directoryObject.getCurrentPath().equals(path)) {
            return;
        }
        swipeRefreshLayout.setRefreshing(false);
        if (fetchDirectoryTask != null) {
            fetchDirectoryTask.cancel(true);
        }
        directoryObject.setPath(path);
        //noinspection StatementWithEmptyBody
        while (!pathStack.pop().equals(path)) {
            // pop stack until we find path
        }
        breadcrumbView.removeCrumbsUpTo(path);
        recyclerViewAdapter.clear();

        if (!directoryObject.isContentValid(path)) {
            swipeRefreshLayout.setRefreshing(true);
            directoryObject.restoreFromCache(path);
            sortDirectory();
            recyclerViewAdapter.newData(directoryObject.getDirectoryContent());
            if (directoryPosition.containsKey(directoryObject.getCurrentPath())) {
                int position = directoryPosition.get(directoryObject.getCurrentPath());
                recyclerViewLinearLayoutManager.scrollToPositionWithOffset(position, 10);
            }
            fetchDirectoryTask = new FetchDirectoryContent(true).execute();
        } else if (directoryObject.isPathInCache(path)) {
            directoryObject.restoreFromCache(path);
            sortDirectory();
            recyclerViewAdapter.newData(directoryObject.getDirectoryContent());
            if (directoryPosition.containsKey(directoryObject.getCurrentPath())) {
                int position = directoryPosition.get(directoryObject.getCurrentPath());
                recyclerViewLinearLayoutManager.scrollToPositionWithOffset(position, 10);
            }
        } else {
            fetchDirectoryTask = new FetchDirectoryContent().execute();
        }
    }

    private void showSyncDialog(String path) {
        syncRemotePath = path;

        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        String[] options = SyncDirectionObject.getOptionsArray(getContext());
        builder.setTitle(R.string.select_sync_direction);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int value = which+1;
                switch (value){
                    case SyncDirectionObject.SYNC_REMOTE_TO_LOCAL: syncDirection = SyncDirectionObject.SYNC_REMOTE_TO_LOCAL; break;
                    case SyncDirectionObject.COPY_LOCAL_TO_REMOTE: syncDirection = SyncDirectionObject.COPY_LOCAL_TO_REMOTE; break;
                    case SyncDirectionObject.COPY_REMOTE_TO_LOCAL: syncDirection = SyncDirectionObject.COPY_REMOTE_TO_LOCAL; break;
                    default: syncDirection = SyncDirectionObject.SYNC_LOCAL_TO_REMOTE; break;
                }

                Intent intent = new Intent(context, FilePicker.class);
                intent.putExtra(FilePicker.FILE_PICKER_PICK_DESTINATION_TYPE, true);
                startActivityForResult(intent, FILE_PICKER_SYNC_RESULT);
            }
        });
        builder.show();
    }

    private void showBottomBar() {
        View bottomBar = ((FragmentActivity) context).findViewById(R.id.bottom_bar);
        if (bottomBar.getVisibility() == View.VISIBLE) {
            return;
        }
        bottomBar.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_animation);
        bottomBar.startAnimation(animation);
    }

    private void hideBottomBar() {
        View bottomBar = ((FragmentActivity) context).findViewById(R.id.bottom_bar);
        if (bottomBar.getVisibility() != View.VISIBLE) {
            return;
        }
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out_animation);
        bottomBar.setAnimation(animation);
        bottomBar.setVisibility(View.GONE);
    }

    private void hideMoveBar() {
        View moveBar = ((FragmentActivity) context).findViewById(R.id.move_bar);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out_animation);
        moveBar.setAnimation(animation);
        moveBar.setVisibility(View.GONE);
    }

    private void deleteFiles(final List<FileItem> deleteList) {
        String title = "Delete " + deleteList.size();
        final String content = (deleteList.size() == 1) ? deleteList.get(0).getName() + " will be deleted" : "";
        title += (deleteList.size() > 1) ? " items?" : " item?";
        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        builder
                .setTitle(title)
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        recyclerViewAdapter.cancelSelection();
                        for (FileItem deleteItem : deleteList) {
                            Intent intent = new Intent(context, DeleteService.class);
                            intent.putExtra(DeleteService.REMOTE_ARG, remote);
                            intent.putExtra(DeleteService.DELETE_ITEM, deleteItem);
                            intent.putExtra(DeleteService.PATH, directoryObject.getCurrentPath());
                            context.startService(intent);
                        }
                        Toasty.info(context, getString(R.string.deleting_info), Toast.LENGTH_SHORT, true).show();
                    }
                });
        if (!content.trim().isEmpty()) {
            builder.setMessage(content);
        }
        builder.create().show();
    }

    private void renameFiles() {
        if (getFragmentManager() != null) {
            new InputDialog()
                    .setTitle(R.string.rename_file)
                    .setMessage(R.string.type_new_file_name)
                    .setNegativeButton(R.string.cancel)
                    .setPositiveButton(R.string.okay_confirmation)
                    .setFilledText(renameItem.getName())
                    .setDarkTheme(isDarkTheme)
                    .setTag("rename file")
                    .show(getChildFragmentManager(), "input dialog");
        }
    }

    private void downloadFiles() {
        Intent intent = new Intent(context, FilePicker.class);
        intent.putExtra(FilePicker.FILE_PICKER_PICK_DESTINATION_TYPE, true);
        startActivityForResult(intent, FILE_PICKER_DOWNLOAD_RESULT);
    }

    private void moveFiles(List<FileItem> moveItems) {
        moveStartPath = directoryObject.getCurrentPath();
        moveList = new ArrayList<>(moveItems);
        recyclerViewAdapter.cancelSelection();
        recyclerViewAdapter.setMoveMode(true);
        isInMoveMode = true;
        showCancelButtonInToolbar();
        ((FragmentActivity) context).setTitle(getString(R.string.select_destination));
        ((FragmentActivity) context).findViewById(R.id.move_bar).setVisibility(View.VISIBLE);
        setOptionsMenuVisibility(false);
        fab.hide();
        fab.setVisibility(View.INVISIBLE);
        setFabBehaviour(false);
    }

    private void onCreateNewDirectory() {
        if (getFragmentManager() != null) {
            new InputDialog()
                    .setTitle(R.string.create_new_folder)
                    .setMessage(R.string.type_new_folder_name)
                    .setNegativeButton(R.string.cancel)
                    .setPositiveButton(R.string.okay_confirmation)
                    .setDarkTheme(isDarkTheme)
                    .setTag("new dir")
                    .show(getChildFragmentManager(), "input dialog");
        }
    }

    /*
     * Input Dialog callback
     */
    @Override
    public void onPositive(String tag, String input) {
        switch (tag) {
            case "new dir":
                if (input.trim().length() == 0) {
                    return;
                }
                String newDir;
                if (directoryObject.getCurrentPath().equals("//" + remoteName)) {
                    newDir = input;
                } else {
                    newDir = directoryObject.getCurrentPath() + "/" + input;
                }
                new MakeDirectoryTask().execute(newDir);
                break;
            case "rename file":
                if (renameItem.getName().equals(input)) {
                    return;
                }
                recyclerViewAdapter.cancelSelection();
                String newFilePath;
                if (directoryObject.getCurrentPath().equals("//" + remoteName)) {
                    newFilePath = input;
                } else {
                    newFilePath = directoryObject.getCurrentPath() + "/" + input;
                }
                new RenameFileTask().execute(renameItem.getPath(), newFilePath);
                renameItem = null;
                break;
        }
    }

    private void onUploadFiles() {
        Intent intent = new Intent(context, FilePicker.class);
        startActivityForResult(intent, FILE_PICKER_UPLOAD_RESULT);
    }

    /*
     * Go To Dialog Callback
     */
    @Override
    public void onRootClicked(boolean isSetAsDefault) {
        startAtRoot = true;
        directoryObject.clear();
        String path = "//" + remoteName;
        directoryObject.setPath(path);
        swipeRefreshLayout.setRefreshing(true);
        fetchDirectoryTask = new FetchDirectoryContent().execute();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isSetAsDefault) {
            editor.putBoolean(getString(R.string.pref_key_go_to_default_set), true);
            editor.putBoolean(getString(R.string.pref_key_start_at_root), true);
        } else {
            editor.putBoolean(getString(R.string.pref_key_go_to_default_set), false);
        }
        editor.apply();
    }

    /*
     * Go To Dialog Callback
     */
    @Override
    public void onHomeClicked(boolean isSetAsDefault) {
        startAtRoot = false;
        directoryObject.clear();
        String path = "//" + remoteName;
        directoryObject.setPath(path);
        swipeRefreshLayout.setRefreshing(true);
        fetchDirectoryTask = new FetchDirectoryContent().execute();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isSetAsDefault) {
            editor.putBoolean(getString(R.string.pref_key_go_to_default_set), true);
            editor.putBoolean(getString(R.string.pref_key_start_at_root), false);
        } else {
            editor.putBoolean(getString(R.string.pref_key_go_to_default_set), false);
        }
        editor.apply();
    }

    /***********************************************************************************************
     * AsyncTask classes
     ***********************************************************************************************/
    @SuppressLint("StaticFieldLeak")
    private class FetchDirectoryContent extends AsyncTask<Void, Void, List<FileItem>> {

        private boolean silentFetch;

        FetchDirectoryContent() {
            this(false);
        }

        FetchDirectoryContent(boolean silentFetch) {
            this.silentFetch = silentFetch;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(true);
            }
        }

        @Override
        protected List<FileItem> doInBackground(Void... voids) {
            List<FileItem> fileItemList;
            fileItemList = rclone.getDirectoryContent(remote, directoryObject.getCurrentPath(), startAtRoot);
            return fileItemList;
        }

        @Override
        protected void onPostExecute(List<FileItem> fileItems) {
            super.onPostExecute(fileItems);
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            if (fileItems == null) {
                if (silentFetch) {
                    return;
                }
                Toasty.error(context, getString(R.string.error_getting_dir_content), Toast.LENGTH_SHORT, true).show();
                fileItems = new ArrayList<>();
            }

            directoryObject.setContent(fileItems);
            sortDirectory();

            if (isSearchMode && searchString != null) {
                searchDirContent(searchString);
            } else {
                if (recyclerViewAdapter != null) {
                    if (silentFetch) {
                        recyclerViewAdapter.updateData(directoryObject.getDirectoryContent());
                    } else {
                        recyclerViewAdapter.newData(directoryObject.getDirectoryContent());
                    }
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }
    
    @SuppressLint("StaticFieldLeak")
    private class RenameFileTask extends AsyncTask<String, Void, Boolean> {

        private String pathWhenTaskStarted;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pathWhenTaskStarted = directoryObject.getCurrentPath();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            String oldFileName = strings[0];
            String newFileName = strings[1];

            return rclone.moveTo(remote, oldFileName, newFileName);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!isRunning) {
                return;
            }
            if (result) {
                Toasty.success(context, getString(R.string.file_renamed_success), Toast.LENGTH_SHORT, true).show();
            } else {
                Toasty.error(context, getString(R.string.error_moving_file), Toast.LENGTH_SHORT, true).show();

            }
            if (!pathWhenTaskStarted.equals(directoryObject.getCurrentPath())) {
                directoryObject.removePathFromCache(pathWhenTaskStarted);
                return;
            }
            if (fetchDirectoryTask != null) {
                fetchDirectoryTask.cancel(true);
            }
            swipeRefreshLayout.setRefreshing(false);

            fetchDirectoryTask = new FetchDirectoryContent(true).execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class MakeDirectoryTask extends AsyncTask<String, Void, Boolean> {

        private String pathWhenTaskStarted;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pathWhenTaskStarted = directoryObject.getCurrentPath();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            String newDir = strings[0];
            return rclone.makeDirectory(remote, newDir);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!isRunning) {
                return;
            }
            if (result) {
                Toasty.success(context, getString(R.string.make_directory_success), Toast.LENGTH_SHORT, true).show();
            } else {
                Toasty.error(context, getString(R.string.error_mkdir), Toast.LENGTH_SHORT, true).show();
            }
            if (!pathWhenTaskStarted.equals(directoryObject.getCurrentPath())) {
                directoryObject.removePathFromCache(pathWhenTaskStarted);
                return;
            }
            swipeRefreshLayout.setRefreshing(false);
            if (fetchDirectoryTask != null) {
                fetchDirectoryTask.cancel(true);
            }
            fetchDirectoryTask = new FetchDirectoryContent(true).execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadAndOpen extends AsyncTask<FileItem, Void, Boolean> {

        public static final int OPEN_AS_TEXT = 1;
        public static final int OPEN_AS_IMAGE = 2;
        private int openAs;
        private LoadingDialog loadingDialog;
        private String fileLocation;
        private Process process;

        DownloadAndOpen() {
            this(-1);
        }

        DownloadAndOpen(int openAs) {
            this.openAs = openAs;
        }

        private void cancelProcess() {
            process.destroy();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog = new LoadingDialog()
                    .setCanCancel(false)
                    .setDarkTheme(isDarkTheme)
                    .setTitle(getString(R.string.loading_file))
                    .setNegativeButton(getResources().getString(R.string.cancel))
                    .setOnNegativeListener(new LoadingDialog.OnNegative() {
                @Override
                public void onNegative() {
                    cancelProcess();
                    cancel(true);
                }
            });
            if (getFragmentManager() != null) {
                loadingDialog.show(getChildFragmentManager(), "loading dialog");
            }
        }

        @Override
        protected Boolean doInBackground(FileItem... fileItems) {
            FileItem fileItem = fileItems[0];
            File file = context.getExternalCacheDir();
            String saveLocation;
            if (file != null) {
                saveLocation = file.getAbsolutePath();
            } else {
                return false;
            }

            fileLocation = saveLocation + "/" + fileItem.getName();

            process = rclone.downloadFile(remote, fileItem, saveLocation);

            if (process != null) {
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            if (process != null && process.exitValue() == 0) {
                File savedFile = new File(fileLocation);
                savedFile.setReadOnly();
            }

            if (process != null && process.exitValue() != 0) {
                rclone.logErrorOutput(process);
            }

            return process != null && process.exitValue() == 0;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            super.onPostExecute(status);
            if (loadingDialog.isStateSaved()) {
                loadingDialog.dismissAllowingStateLoss();
            } else {
                try {
                    loadingDialog.dismiss();
                } catch (NullPointerException e) {
                    return;
                }
            }
            if (!status) {
                return;
            }
            Uri sharedFileUri = FileProvider.getUriForFile(context, "ca.pkay.rcloneexplorer.fileprovider", new File(fileLocation));
            Intent intent = new Intent(Intent.ACTION_VIEW, sharedFileUri);

            if (openAs == OPEN_AS_TEXT) {
                intent.setDataAndType(sharedFileUri,"text/*");
            } else if (openAs == OPEN_AS_IMAGE) {
                intent.setDataAndType(sharedFileUri, "image/*");
            } else {
                String extension = MimeTypeMap.getFileExtensionFromUrl(sharedFileUri.toString());
                String type = context.getContentResolver().getType(sharedFileUri);
                if (extension == null || extension.trim().isEmpty()) {
                    intent.setDataAndType(sharedFileUri, "*/*");
                } else if (type == null || type.equals("application/octet-stream")) {
                    intent.setDataAndType(sharedFileUri, "*/*");
                }
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class StreamTask extends AsyncTask<FileItem, Void, Void> {

        public static final int OPEN_AS_VIDEO = 0;
        public static final int OPEN_AS_AUDIO = 1;
        private int openAs;
        private LoadingDialog loadingDialog;

        StreamTask() {
            this(-1);
        }

        StreamTask(int openAs) {
            this.openAs = openAs;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog = new LoadingDialog()
                    .setCanCancel(false)
                    .setDarkTheme(isDarkTheme)
                    .setTitle(R.string.loading);

            if (getFragmentManager() != null) {
                loadingDialog.show(getChildFragmentManager(), "loading dialog");
            }
        }

        @Override
        protected Void doInBackground(FileItem... fileItems) {
            FileItem fileItem = fileItems[0];

            Intent serveIntent = new Intent(getContext(), StreamingService.class);
            serveIntent.putExtra(StreamingService.SERVE_PATH_ARG, fileItem.getPath());
            serveIntent.putExtra(StreamingService.REMOTE_ARG, remote);
            serveIntent.putExtra(StreamingService.SHOW_NOTIFICATION_TEXT, false);
            context.startService(serveIntent);

            Uri uri = Uri.parse("http://127.0.0.1:8080")
                    .buildUpon()
                    .appendPath(fileItem.getName())
                    .build();

            Intent intent = new Intent(Intent.ACTION_VIEW);

            // open as takes precedence
            if (openAs == OPEN_AS_VIDEO) {
                intent.setDataAndType(uri, "video/*");
            } else if (openAs == OPEN_AS_AUDIO) {
                intent.setDataAndType(uri, "audio/*");
            } else {
                String type = fileItem.getMimeType();
                if (type.startsWith("audio/")) {
                    intent.setDataAndType(uri, "audio/*");
                } else if (type.startsWith("video/")) {
                    intent.setDataAndType(uri, "video/*");
                } else {
                    intent.setData(uri);
                }
            }

            int code = -1;
            HttpURLConnection connection;

            int retries = 10;
            while (retries > 0) {
                try {
                    URL checkUrl = new URL(uri.toString());
                    connection = (HttpURLConnection) checkUrl.openConnection();
                    code = connection.getResponseCode();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (code == 200) {
                    break;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                retries--;
            }

            if (loadingDialog != null) {
                if (loadingDialog.isStateSaved()) {
                    loadingDialog.dismissAllowingStateLoss();
                } else {
                    loadingDialog.dismiss();
                }
            }
            startActivityForResult(intent, STREAMING_INTENT_RESULT);
            return null;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class EmptyTrashTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            return rclone.emptyTrashCan(remoteName);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (isRunning) {
                if (result) {
                    Toasty.success(context, getString(R.string.trash_emptied), Toast.LENGTH_SHORT, true).show();
                } else {
                    Toasty.error(context, getString(R.string.error_emptying_trash), Toast.LENGTH_SHORT, true).show();
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class LinkTask extends AsyncTask<String, Void, String> {

        private LoadingDialog loadingDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog = new LoadingDialog()
                    .setTitle(R.string.generating_public_link)
                    .setDarkTheme(isDarkTheme);
            if (getFragmentManager() != null) {
                loadingDialog.show(getChildFragmentManager(), "loading dialog");
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            String linkPath = strings[0];
            return rclone.link(remote, linkPath);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (loadingDialog != null) {
                if (loadingDialog.isStateSaved()) {
                    loadingDialog.dismissAllowingStateLoss();
                } else {
                    loadingDialog.dismiss();
                }
            }

            if (s == null) {
                Toasty.error(context, getString(R.string.error_generating_link), Toast.LENGTH_SHORT, true).show();
                return;
            }

            LinkDialog linkDialog = new LinkDialog()
                    .isDarkTheme(isDarkTheme)
                    .setLinkUrl(s);
            if (getFragmentManager() != null) {
                linkDialog.show(getChildFragmentManager(), "link dialog");
            }

            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("Copied link", s);
            if (clipboardManager == null) {
                return;
            }
            clipboardManager.setPrimaryClip(clipData);
            Toasty.info(context, getString(R.string.link_copied_to_clipboard), Toast.LENGTH_SHORT, true).show();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (loadingDialog.isStateSaved()) {
                loadingDialog.dismissAllowingStateLoss();
            } else {
                loadingDialog.dismiss();
            }
        }
    }
}
