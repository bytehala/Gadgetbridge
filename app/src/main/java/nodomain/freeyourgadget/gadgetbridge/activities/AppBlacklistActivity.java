package nodomain.freeyourgadget.gadgetbridge.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;


public class AppBlacklistActivity extends AppCompatActivity {
    private static final Logger LOG = LoggerFactory.getLogger(AppBlacklistActivity.class);

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(GBApplication.ACTION_QUIT)) {
                finish();
            }
        }
    };

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appblacklist);

        final PackageManager pm = getPackageManager();

        final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        final List<AppInfoWrapper> packageList = new ArrayList<>();
        for(ApplicationInfo info : packages) {
            packageList.add(new AppInfoWrapper(info, pm));
        }
        ListView appListView = (ListView) findViewById(R.id.appListView);

        final ArrayAdapter<AppInfoWrapper> adapter = new ArrayAdapter<AppInfoWrapper>(this, R.layout.item_with_checkbox, packageList) {
            @Override
            public View getView(int position, View view, ViewGroup parent) {
                if (view == null) {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                adapter.notifyDataSetChanged();
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(GBApplication.ACTION_QUIT);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}
