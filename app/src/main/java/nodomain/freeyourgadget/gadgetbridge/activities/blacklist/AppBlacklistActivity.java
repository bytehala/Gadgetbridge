package nodomain.freeyourgadget.gadgetbridge.activities.blacklist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;


public class AppBlacklistActivity extends AppCompatActivity  implements OnAppListingChangedListener {
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new AppListPagerAdapter(getSupportFragmentManager(),
                this));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_appblacklist);
//
//        final PackageManager pm = getPackageManager();
//
//        final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
//        final List<AppInfoWrapper> packageList = new ArrayList<>();
//        for(ApplicationInfo info : packages) {
//            packageList.add(new AppInfoWrapper(info, pm));
//        }
//        ListView appListView = (ListView) findViewById(R.id.appListView);
//
//        final ArrayAdapter<AppInfoWrapper> adapter = new ArrayAdapter<AppInfoWrapper>(this, R.layout.item_with_checkbox, packageList) {
//            @Override
//            public View getView(int position, View view, ViewGroup parent) {
//                if (view == null) {
//                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                    view = inflater.inflate(R.layout.item_with_checkbox, parent, false);
//                }
//
//                AppInfoWrapper appInfo = packageList.get(position);
//                TextView deviceAppVersionAuthorLabel = (TextView) view.findViewById(R.id.item_details);
//                TextView deviceAppNameLabel = (TextView) view.findViewById(R.id.item_name);
//                ImageView deviceImageView = (ImageView) view.findViewById(R.id.item_image);
//                CheckBox checkbox = (CheckBox) view.findViewById(R.id.item_checkbox);
//
//                deviceAppVersionAuthorLabel.setText(appInfo.packageName);
//                deviceAppNameLabel.setText(appInfo.getAppName());
//                deviceImageView.setImageDrawable(appInfo.getIcon());
//
//                checkbox.setChecked(GBApplication.blacklist.contains(appInfo.packageName));
//
//                Collections.sort(packageList, new Comparator<AppInfoWrapper>() {
//                    @Override
//                    public int compare(AppInfoWrapper ai1, AppInfoWrapper ai2) {
//                        boolean blacklisted1 = GBApplication.blacklist.contains(ai1.packageName);
//                        boolean blacklisted2 = GBApplication.blacklist.contains(ai2.packageName);
//
//                        if ((blacklisted1 && blacklisted2) || (!blacklisted1 && !blacklisted2)) {
//                            // both blacklisted or both not blacklisted = sort by alphabet
//                            return ai1.packageName.compareTo(ai2.packageName);
//                        } else if (blacklisted1) {
//                            return -1;
//                        } else {
//                            return 1;
//                        }
//                    }
//                });
//                return view;
//            }
//        };
//        appListView.setAdapter(adapter);
//
//        appListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView parent, View v, int position, long id) {
//                String packageName = packageList.get(position).packageName;
//                CheckBox checkBox = ((CheckBox) v.findViewById(R.id.item_checkbox));
//                checkBox.toggle();
//                if (checkBox.isChecked()) {
//                    GBApplication.addToBlacklist(packageName);
//                } else {
//                    GBApplication.removeFromBlacklist(packageName);
//                }
//                adapter.notifyDataSetChanged();
//            }
//        });
//
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(GBApplication.ACTION_QUIT);
//
    // TODO This BroadcastManager can.
//        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
//    }

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

    @Override
    public void blacklist(String packageName) {
        // TODO This can be improved
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        AppList blackAppList = (AppList) fragmentList.get(0);
        blackAppList.addToList(packageName);
    }

    @Override
    public void whitelist(String packageName) {
        // TODO This can be improved
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        AppList whiteAppList = (AppList) fragmentList.get(1);
        whiteAppList.addToList(packageName);
    }
}
