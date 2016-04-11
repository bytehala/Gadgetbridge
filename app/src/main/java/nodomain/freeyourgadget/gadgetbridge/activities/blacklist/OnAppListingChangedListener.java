package nodomain.freeyourgadget.gadgetbridge.activities.blacklist;

public interface OnAppListingChangedListener {

    public void blacklist(String packageName);
    public void whitelist(String packageName);

}
