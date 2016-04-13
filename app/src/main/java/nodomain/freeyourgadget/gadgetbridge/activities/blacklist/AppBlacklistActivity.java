package nodomain.freeyourgadget.gadgetbridge.activities.blacklist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

    private AppList blacklist;
    private AppList whitelist;

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

    @Override
    public void whitelist(String packageName) {
        whitelist = getWhitelist();
        if (whitelist != null) {
            whitelist.addToList(packageName);
        }
    }

    @Override
    public void blacklist(String packageName) {
        blacklist = getBlacklist();
        if (blacklist != null) {
            blacklist.addToList(packageName);
        }
    }

    // TODO This can be improved
    @Nullable
    private AppList getBlacklist() {
        if (blacklist == null || blacklist.getIdentity() != AppListFragment.Identity.BLACKLIST) {
            List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
            for(Fragment fragment : fragmentList) {
                AppList appList = (AppList) fragment;
                if (appList.getIdentity() == AppListFragment.Identity.BLACKLIST) {
                    blacklist = appList;
                    return blacklist;
                }
            }
        }
        return blacklist;
    }

    // TODO This can be improved
    @Nullable
    private AppList getWhitelist() {
        if (whitelist == null || whitelist.getIdentity() != AppListFragment.Identity.WHITELIST) {
            List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
            for(Fragment fragment : fragmentList) {
                AppList appList = (AppList) fragment;
                if (appList.getIdentity() == AppListFragment.Identity.WHITELIST) {
                    whitelist = appList;
                    return whitelist;
                }
            }
        }
        return whitelist;
    }
}
