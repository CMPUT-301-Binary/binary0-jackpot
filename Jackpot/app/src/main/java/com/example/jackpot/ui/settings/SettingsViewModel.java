package com.example.jackpot.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The model for the settings fragment.
 */
public class SettingsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    /**
     * Constructor for the settings view model.
     */
    public SettingsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is settings fragment");
    }

    /**
     * Getter for the text.
     *
     * @return the text to display
     */
    public LiveData<String> getText() {
        return mText;
    }
}