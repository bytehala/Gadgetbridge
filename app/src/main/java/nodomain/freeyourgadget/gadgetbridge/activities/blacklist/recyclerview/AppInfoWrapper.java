package nodomain.freeyourgadget.gadgetbridge.activities.blacklist.recyclerview;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

// TODO Proper javadoc
/* This class exists because otherwise, calls to ApplicationInfo#loadLabel would slow down
 * rendering
 */
public class AppInfoWrapper {


    public final String packageName;
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

    public CharSequence getAppName() {
        return appName;
    }

}
