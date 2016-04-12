package nodomain.freeyourgadget.gadgetbridge.activities.blacklist.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.InfoWrapperViewHolder> {

    private List<AppInfoWrapper> items;

    public AppListAdapter(List<AppInfoWrapper> modelData) {
        if (modelData == null) {
            throw new IllegalArgumentException(
                    "modelData must not be null");
        }
        this.items = modelData;
    }


    @Override
    public InfoWrapperViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.item_with_checkbox,
                        viewGroup,
                        false);
        return new InfoWrapperViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(InfoWrapperViewHolder holder, int position) {
        AppInfoWrapper model = items.get(position);
        holder.packageText.setText(model.packageName);
        holder.appNameText.setText(model.getAppName());
        holder.appIcon.setImageDrawable(model.getIcon());

        // TODO: Set this to the passed Adapter Identity, not blacklist.contains
        model.setChecked(GBApplication.blacklist.contains(model.packageName));


    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static final class InfoWrapperViewHolder extends RecyclerView.ViewHolder {


        TextView packageText;
        TextView appNameText;
        ImageView appIcon;
        CheckBox checkbox;

        public InfoWrapperViewHolder(View view) {
            super(view);
            packageText = (TextView) view.findViewById(R.id.item_details);
            appNameText = (TextView) view.findViewById(R.id.item_name);
            appIcon = (ImageView) view.findViewById(R.id.item_image);
            checkbox = (CheckBox) view.findViewById(R.id.item_checkbox);
        }
    }
}
