package navView.serializables;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.res.ColorStateList;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.io.Serializable;

import navView.utils.NavViewUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on July 13, 2026
 */
public class NavViewEntry implements Serializable {

    public interface FragmentSupplier {
        Fragment create();
    }

    private final transient FragmentSupplier supplier;
    private final int drawableRes;
    private final String titles;

    public NavViewEntry(FragmentSupplier supplier, int drawableRes) {
        this.supplier = supplier;
        this.drawableRes = drawableRes;
        this.titles = null;
    }

    public FragmentSupplier getSupplier() {
        return supplier;
    }

    public void load(MaterialButton button, boolean isSelected) {
        if (this.drawableRes != Integer.MIN_VALUE) {
            button.setIconResource(this.drawableRes);
        }
        if (this.titles != null) {
            button.setText(this.titles);
        }
        int activeColor = NavViewUtils.getMaterialColorActive(button.getContext());
        int inactiveColor = NavViewUtils.getMaterialColorInactive(button.getContext());

        button.setIconTint(ColorStateList.valueOf(isSelected ? activeColor : inactiveColor));
        button.setTextColor(isSelected ? activeColor : inactiveColor);

        button.setChecked(isSelected);

        if (button.getVisibility() == GONE) button.setVisibility(VISIBLE);
    }

}