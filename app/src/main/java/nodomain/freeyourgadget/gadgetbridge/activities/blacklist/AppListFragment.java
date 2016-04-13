package nodomain.freeyourgadget.gadgetbridge.activities.blacklist;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.blacklist.recyclerview.AppInfoWrapper;
import nodomain.freeyourgadget.gadgetbridge.activities.blacklist.recyclerview.AppListAdapter;
import nodomain.freeyourgadget.gadgetbridge.common.ItemClickSupport;

public class AppListFragment extends Fragment implements AppList {

    public static final String ARG_PAGE = "ARG_PAGE";

    OnAppListingChangedListener mCallback;
    private PackageManager pm;
    private ProgressBar appListLoading;

    // TODO: Enums are bad. Refactor.
    public enum Identity {
        BLACKLIST,
        WHITELIST
    }

    private int mPage;
    private RecyclerView.Adapter adapter;
    private Identity identity;

    // TODO Save fragment contents for rotation.

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        pm = getActivity().getPackageManager();
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

        // TODO packageList here
        packageList = new ArrayList<>();

        adapter = new AppListAdapter(packageList, identity);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                Collections.sort(packageList);
                super.onChanged();
            }
        });
        new AppListQueryTask().execute(identity);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_applist, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.appListView);
        appListLoading = (ProgressBar) view.findViewById(R.id.appListLoading);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                if (position == -1) {
                    return;
                }

                CheckBox checkBox = ((CheckBox) v.findViewById(R.id.item_checkbox));
                checkBox.toggle();

                String packageName = packageList.get(position).packageName;
                if (identity == Identity.WHITELIST) {
                    GBApplication.addToBlacklist(packageName);
                    mCallback.blacklist(packageName);
                } else {
                    GBApplication.removeFromBlacklist(packageName);
                    mCallback.whitelist(packageName);
                }
                packageList.remove(position);
                adapter.notifyItemRemoved(position);

            }
        });

        return view;
    }

    @Override
    public void addToList(String packageName) {
        ApplicationInfo info = getApplicationInfo(packageName);
        if (info != null) {
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

    private class AppListQueryTask extends AsyncTask<Identity, Void, List<AppInfoWrapper>> {

        @Override
        protected List<AppInfoWrapper> doInBackground(Identity... params) {
            Identity identity = params[0];
            if (identity == Identity.WHITELIST) {
                return getWhiteList();
            } else {
                return getBlacklist();
            }
        }

        @Override
        protected void onPostExecute(List<AppInfoWrapper> result) {
            packageList.addAll(result);
            adapter.notifyDataSetChanged();
            appListLoading.setVisibility(View.INVISIBLE);
        }


        private List<AppInfoWrapper> getBlacklist() {
            Set<String> packageNames = GBApplication.blacklist;
            List<AppInfoWrapper> result = new ArrayList<>();
            for (String packageName : packageNames) {
                ApplicationInfo info = getApplicationInfo(packageName);
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
    }


}
