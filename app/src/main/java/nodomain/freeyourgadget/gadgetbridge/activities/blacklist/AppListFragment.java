package nodomain.freeyourgadget.gadgetbridge.activities.blacklist;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;

public class AppListFragment extends Fragment implements AppList {

    public static final String ARG_PAGE = "ARG_PAGE";

    OnAppListingChangedListener mCallback;
    private PackageManager pm;

    enum Identity {
        BLACKLIST,
        WHITELIST
    }

    private int mPage;
    private ArrayAdapter<AppInfoWrapper> adapter;
    private Identity identity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        pm = getActivity().getPackageManager(); // TODO If this is null, use context
        try {
            mCallback = (OnAppListingChangedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnAppListingChangedListener");
        }
    }

    private List<AppInfoWrapper> packageList;

    public static AppListFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        AppListFragment fragment = new AppListFragment();
        fragment.setArguments(args);

        fragment.identity = page == 1 ? Identity.WHITELIST : Identity.BLACKLIST;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);

        if (identity == Identity.WHITELIST) {
            packageList = getWhiteList();
        } else {
            packageList = getBlacklist();
        }

        // both blacklisted or both not blacklisted = sort by alphabet
        adapter = new ArrayAdapter<AppInfoWrapper>(getActivity(), R.layout.item_with_checkbox, packageList) {
            @Override
            public View getView(int position, View view, ViewGroup parent) {
                if (view == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = inflater.inflate(R.layout.item_with_checkbox, parent, false);
                }

                AppInfoWrapper appInfo = packageList.get(position);
                TextView deviceAppVersionAuthorLabel = (TextView) view.findViewById(R.id.item_details);
                TextView deviceAppNameLabel = (TextView) view.findViewById(R.id.item_name);
                ImageView deviceImageView = (ImageView) view.findViewById(R.id.item_image);
                CheckBox checkbox = (CheckBox) view.findViewById(R.id.item_checkbox);

                deviceAppVersionAuthorLabel.setText(appInfo.packageName);
                deviceAppNameLabel.setText(appInfo.getAppName());
                deviceImageView.setImageDrawable(appInfo.getIcon());

                checkbox.setChecked(GBApplication.blacklist.contains(appInfo.packageName));

                Collections.sort(packageList, new Comparator<AppInfoWrapper>() {
                    @Override
                    public int compare(AppInfoWrapper ai1, AppInfoWrapper ai2) {
                        boolean blacklisted1 = GBApplication.blacklist.contains(ai1.packageName);
                        boolean blacklisted2 = GBApplication.blacklist.contains(ai2.packageName);

                        if ((blacklisted1 && blacklisted2) || (!blacklisted1 && !blacklisted2)) {
                            // both blacklisted or both not blacklisted = sort by alphabet
                            return ai1.packageName.compareTo(ai2.packageName);
                        } else if (blacklisted1) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });
                return view;
            }
        };


    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ListView appListView = identity == Identity.WHITELIST ?
                (ListView) inflater.inflate(R.layout.fragment_applist, container, false) :
                (ListView) inflater.inflate(R.layout.fragment_blacklist, container, false);

        appListView.setAdapter(adapter);

        appListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                String packageName = packageList.get(position).packageName;
                CheckBox checkBox = ((CheckBox) v.findViewById(R.id.item_checkbox));
                checkBox.toggle();

                if (checkBox.isChecked()) {
                    GBApplication.addToBlacklist(packageName);
                } else {
                    GBApplication.removeFromBlacklist(packageName);
                }
                adapter.remove(adapter.getItem(position));
                adapter.notifyDataSetChanged();
                if (identity == Identity.WHITELIST) {
                    mCallback.whitelist(packageName);
                } else {
                    mCallback.blacklist(packageName);
                }
            }
        });

        // TODO Add Quit BroadcastListener
        return appListView;
    }

    @Override
    public void addToList(String packageName) {
        ApplicationInfo info = getApplicationInfo(packageName);
        if(info != null) {
            packageList.add(new AppInfoWrapper(info, pm));
            adapter.notifyDataSetChanged();
        }
    }

    private ApplicationInfo getApplicationInfo(String packageName) {
        ApplicationInfo result = null;
        try {
            result = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace(); // TODO DO NOT DO THIS.
        }
        return result;
    }

    private List<AppInfoWrapper> getBlacklist() {
        Set<String> packageNames = GBApplication.blacklist;
        List<AppInfoWrapper>  result = new ArrayList<>();
        for (String packageName : packageNames) {
            ApplicationInfo info =  getApplicationInfo(packageName);
            if (info != null) {
                result.add(new AppInfoWrapper(info, pm));
            }
        }
        return result;
    }

    private List<AppInfoWrapper> getWhiteList() {
        List<ApplicationInfo> infoList = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<AppInfoWrapper> result = new ArrayList<>();
        for (ApplicationInfo info : infoList) {
            if (!GBApplication.blacklist.contains(info.packageName)) {
                result.add(new AppInfoWrapper(info, pm));
            }
        }
        return result;
    }

    private static class AppInfoWrapper {

        private final String packageName;
        private final CharSequence appName;
        private final Drawable icon;
        private boolean isChecked;

        public AppInfoWrapper(ApplicationInfo info, PackageManager pm) {
            packageName = info.packageName;
            appName = info.loadLabel(pm);
            icon = info.loadIcon(pm);
            isChecked = false;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }

        public boolean getChecked() {
            return isChecked;
        }

        public Drawable getIcon() {
            return icon;
        }

        public String getPackageName() {
            return packageName;
        }

        public CharSequence getAppName() {
            return appName;
        }

    }
}
