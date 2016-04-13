package nodomain.freeyourgadget.gadgetbridge.activities.blacklist.recyclerview;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

// TODO Proper javadoc
/* This class exists because otherwise, calls to ApplicationInfo#loadLabel would slow down
 * rendering
 */
public class AppInfoWrapper implements Comparable<AppInfoWrapper> {


    public final String packageName;
    private final CharSequence appName;
    private final Drawable icon;

    public AppInfoWrapper(ApplicationInfo info, PackageManager pm) {
        packageName = info.packageName;
        appName = info.loadLabel(pm);
        icon = info.loadIcon(pm);
    }

    public Drawable getIcon() {
        return icon;
    }

    public CharSequence getAppName() {
        return appName;
    }

    @Override
    public int compareTo(AppInfoWrapper another) {
        return appName.toString().compareToIgnoreCase(another.appName.toString());
    }
}
