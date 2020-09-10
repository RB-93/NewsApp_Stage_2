package com.rohit.examples.android.thepigeonletters.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;

import com.rohit.examples.android.thepigeonletters.R;

/**
 * Class definition where Settings allow users to change the functionality and behavior of an application
 * Settings can affect background behavior, such as how often the application synchronizes data with the cloud,
 * or they can be more wide-reaching, such as changing the contents and presentation of the user interface.
 */
public class SettingsActivity extends AppCompatActivity {

    /**
     * onCreate called to set settings activity layout
     *
     * @param savedInstanceState will be non-null if onSavedInstanceState() was previously called
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    /**
     * Handling Preference options when Back button on touch panel is pressed (instead of Up button)
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }

    /**
     * Showing a hierarchy of NewsPreference objects as lists
     * These preferences will automatically save to SharedPreferences as the user interacts with them
     */
    public static class NewsPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //Inflating the given XML resource and adding the preference hierarchy to the current preference hierarchy.
            addPreferencesFromResource(R.xml.settings_main);

            //Finding reference to search_by preference based on its key
            Preference searchBy = findPreference(getString(R.string.settings_search_by_key));
            //Updating summary value for search_by preference
            bindPreferenceSummaryToValue(searchBy);

            //Finding reference to sort_news_by preference based on its key
            Preference sortNewsBy = findPreference(getString(R.string.settings_sort_by_key));
            //Updating summary value for sort_news_by preference
            bindPreferenceSummaryToValue(sortNewsBy);
        }

        /**
         * Displaying value of preference as a summary under preference
         *
         * @param preference reference to preference widget
         */
        private void bindPreferenceSummaryToValue(Preference preference) {
            //Setting preference change listener to fetch the updated preference value when changed
            preference.setOnPreferenceChangeListener(this);

            //Getting the stored values from SharedPreferences
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());

            //Getting preference empty key-values, if none selected
            String preferenceString = sharedPreferences.getString(preference.getKey(), "");

            //Invoking onPreferenceChange to update the summary with the new value
            onPreferenceChange(preference, preferenceString);
        }

        /**
         * Called when a Preference has been changed by the user
         * This is called before the state of the Preference is about to be updated and before the state is persisted
         *
         * @param preference the changed Preference
         * @param newValue   the new value of the Preference
         * @return returns True to update the state of the Preference with the new value
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            //Identifying if the preference widget is for sort_by ListPreference or search_by EditTextPreference
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                //Fetching the index of the sort_by preference value
                int prefIndex = listPreference.findIndexOfValue(newValue.toString());
                //Updating the summary value of preference value
                if (prefIndex >= 0) {
                    preference.setSummary(listPreference.getEntries()[prefIndex]);
                }
            } else {
                //Setting the summary to be the value of EditText for the EditTextPreference widget
                preference.setSummary(newValue.toString());
            }
            return true;
        }
    }
}