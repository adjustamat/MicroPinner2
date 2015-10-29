package de.dotwee.micropinner.presenter;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import de.dotwee.micropinner.tools.PinHandler;
import de.dotwee.micropinner.view.MainActivity;

/**
 * Created by Lukas Wolfsteiner on 29.10.2015.
 */
public interface MainPresenter {
    String LOG_TAG = "MainPresenter";

    /**
     * This method handles a click on a checkbox.
     *
     * @param checked The state of a checkbox.
     */
    void onCheckBoxClick(boolean checked);

    /**
     * This method handles a long-click on a switch
     */
    void onSwitchHold();

    /**
     * This method handles the click on the positive dialog button.
     */
    void onButtonPositive();

    /**
     * This method handles the click on the negative dialog button.
     */
    void onButtonNegative();

    /**
     * This method handles the expand action.
     *
     * @param expand If view should expand or not.
     */
    void onViewExpand(boolean expand);

    /**
     * This method checks if a parent pin exists.
     */
    boolean hasParentPin();

    /**
     * This method creates a {@link PinHandler.Pin} from the view.
     *
     * @return Null or new {@link PinHandler.Pin}.
     */
    @Nullable
    PinHandler.Pin toPin();

    /**
     * This method returns the corresponding view of the presenter.
     *
     * @return A non null {@link android.support.v7.app.AppCompatActivity} activity.
     */
    @NonNull
    MainActivity getView();

    /**
     * This method notifies all views about the parent pin.
     */
    void notifyAboutParentPin();

    /**
     * Created by Lukas Wolfsteiner on 29.10.2015.
     */
    interface Listeners {
        String LOG_TAG = "Listeners";

        /**
         * This method applies a click-listener to its given view-ids.
         *
         * @param ids The ids to set a click-listener on.
         */
        void setOnClickListener(@NonNull @IdRes final int... ids);

        /**
         * This method applies a long-click-listener to its given view-ids.
         *
         * @param ids The ids to set a long-click-listener on.
         */
        void setOnLongClickListener(@NonNull @IdRes final int... ids);

        /**
         * This method applies a checked-change-listener to its given view-ids.
         *
         * @param ids The ids to set a checked-change-listener on.
         */
        void setOnCheckedChangeListener(@NonNull @IdRes final int... ids);
    }

    /**
     * Created by Lukas Wolfsteiner on 29.10.2015.
     */
    interface Data {
        String LOG_TAG = "Data";

        /**
         * This method reads the value of the visibility spinner widget.
         *
         * @return Value of the content visibility spinner widget.
         */
        int getVisibility();

        /**
         * This method reads the value of the priority spinner widget.
         *
         * @return Value of the content priority spinner widget.
         */
        int getPriority();

        /**
         * This method reads the value of the title editText widget.
         *
         * @return Value of the content title widget.
         */
        String getPinTitle();

        /**
         * This method reads the value of the content editText widget.
         *
         * @return Value of the content editText widget.
         */
        String getPinContent();

        /**
         * This method reads the state of the persistent checkbox widget.
         *
         * @return State of the persistent checkbox.
         */
        boolean isPersistent();
    }
}