/*******************************************************************************
 * Mirakel is an Android App for managing your ToDo-Lists
 *
 * Copyright (c) 2013-2014 Anatolij Zelenin, Georg Semmler.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package de.azapps.mirakel.settings;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.support.annotation.NonNull;
import android.support.v4.content.Loader;
import android.util.SparseBooleanArray;
import android.widget.DatePicker;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.google.common.base.Optional;

import java.util.Calendar;
import java.util.GregorianCalendar;

import de.azapps.mirakel.helper.DateTimeHelper;
import de.azapps.mirakel.helper.MirakelCommonPreferences;
import de.azapps.mirakel.model.MirakelInternalContentProvider;
import de.azapps.mirakel.model.account.AccountMirakel;
import de.azapps.mirakel.model.query_builder.MirakelQueryBuilder;
import de.azapps.mirakel.model.recurring.Recurring;
import de.azapps.mirakel.settings.ListSettings;
import de.azapps.mirakel.settings.NumPickerPref;
import de.azapps.mirakel.settings.R;
import de.azapps.mirakel.settings.generic_list.GenericListSettingActivity;
import de.azapps.mirakel.settings.generic_list.GenericSettingsFragment;
import de.azapps.tools.Log;

public class RecurringSettingsActivity extends GenericListSettingActivity<Recurring> {
    private static final String TAG = "RecurringSettingsActivity";

    @Override
    protected void createModel() {
        Recurring recurring = Recurring.newRecurring(getString(R.string.new_recurring), 0, 0,
                              0, 0, 1, true, Optional.<Calendar>absent(), Optional.<Calendar>absent(), false, false,
                              new SparseBooleanArray());
        super.selectItem(recurring);
    }


    @NonNull
    @Override
    public String getTitle(Optional<Recurring> model) {
        if (model.isPresent()) {
            return model.get().getName();
        } else {
            return getString(R.string.no_recurrence_selected);
        }
    }

    @Override
    public int getPreferenceResource() {
        return R.xml.settings_recurring;
    }

    @Override
    public Uri getUri() {
        return MirakelInternalContentProvider.RECURRING_URI;
    }

    @Override
    public Class<Recurring> getMyClass() {
        return Recurring.class;
    }

    @Override
    public void setUp(final Optional<Recurring> model, final GenericSettingsFragment fragment) {
        if (!model.isPresent()) {
            return;
        }
        final Recurring recurring = model.get();
        final NumPickerPref recurring_year = (NumPickerPref) fragment.findPreference("recurring_year");
        final NumPickerPref recurring_month = (NumPickerPref) fragment.findPreference("recurring_month");
        final NumPickerPref recurring_day = (NumPickerPref) fragment.findPreference("recurring_day");
        final NumPickerPref recurring_hour = (NumPickerPref) fragment.findPreference("recurring_hour");
        final NumPickerPref recurring_minute = (NumPickerPref) fragment.findPreference("recurring_min");
        final CheckBoxPreference forDue = (CheckBoxPreference) fragment.findPreference("forDue");
        final EditTextPreference labelRecurring = (EditTextPreference)
                fragment.findPreference("labelRecurring");
        final Preference startDate = fragment.findPreference("recurring_begin");
        final Preference endDate = fragment.findPreference("recurring_end");
        final String begin = getString(R.string.beginning);
        final String end = getString(R.string.end);
        startDate
        .setSummary(!recurring.getStartDate().isPresent() ? getString(R.string.no_begin_end, begin)
                    : DateTimeHelper.formatDate(recurring
                                                .getStartDate().get(), getString(R.string.humanDateTimeFormat)));
        endDate.setSummary(!recurring.getEndDate().isPresent() ? getString(R.string.no_begin_end,
                           end) : DateTimeHelper
                           .formatDate(recurring.getEndDate().get(),
                                       getString(R.string.humanDateTimeFormat)));
        recurring_day.setValue(recurring.getYears());
        recurring_hour.setValue(recurring.getHours());
        recurring_minute.setValue(recurring.getMinutes());
        recurring_month.setValue(recurring.getMonths());
        recurring_year.setValue(recurring.getYears());
        setSummary(recurring_day, R.plurals.every_days,
                   recurring.getDays());
        setSummary(recurring_month, R.plurals.every_months,
                   recurring.getMonths());
        setSummary(recurring_year, R.plurals.every_years,
                   recurring.getYears());
        setSummary(recurring_hour, R.plurals.every_hours,
                   recurring.getHours());
        setSummary(recurring_minute, R.plurals.every_minutes,
                   recurring.getMinutes());
        hideForReminder(fragment, recurring.isForDue(), recurring_minute,
                        recurring_hour);
        forDue.setChecked(recurring.isForDue());
        labelRecurring.setText(recurring.getLabel());
        labelRecurring.setSummary(recurring.getLabel());
        recurring_day
        .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(
                final Preference preference, final Object newValue) {
                recurring.setDays(recurring_day
                                  .getValue());
                setSummary(recurring_day, R.plurals.every_days,
                           recurring.getDays());
                recurring.save();
                return false;
            }
        });
        recurring_month
        .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(
                final Preference preference, final Object newValue) {
                Log.d(TAG, "change");
                recurring
                .setMonths(recurring_month.getValue());
                setSummary(recurring_month, R.plurals.every_months,
                           recurring.getMonths());
                recurring.save();
                return false;
            }
        });
        recurring_year
        .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(
                final Preference preference, final Object newValue) {
                recurring
                .setYears(recurring_year.getValue());
                setSummary(recurring_year, R.plurals.every_years,
                           recurring.getYears());
                recurring.save();
                return false;
            }
        });
        recurring_hour
        .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(
                final Preference preference, final Object newValue) {
                recurring
                .setHours(recurring_hour.getValue());
                setSummary(recurring_hour, R.plurals.every_hours,
                           recurring.getHours());
                recurring.save();
                return false;
            }
        });
        recurring_minute
        .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(
                final Preference preference, final Object newValue) {
                recurring
                .setMinutes(recurring_minute.getValue());
                setSummary(recurring_minute, R.plurals.every_minutes,
                           recurring.getMinutes());
                recurring.save();
                return false;
            }
        });
        labelRecurring
        .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @SuppressLint("NewApi")
            @Override
            public boolean onPreferenceChange(
                final Preference preference, final Object newValue) {
                recurring.setLabel(newValue
                                   .toString());
                preference.setSummary(recurring
                                      .getLabel());
                preference.setPersistent(false);
                ((EditTextPreference) preference)
                .setText(recurring
                         .getLabel());
                recurring.save();
                return false;
            }
        });
        forDue.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference,
                                              final Object newValue) {
                recurring.setForDue((Boolean) newValue);
                hideForReminder(fragment, recurring.isForDue(),
                                recurring_minute, recurring_hour);
                preference.setPersistent(false);
                ((CheckBoxPreference) preference)
                .setChecked(recurring.isForDue());
                recurring.save();
                return false;
            }
        });
        startDate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                handleDateDialog(recurring, true, startDate, begin);
                return false;
            }
        });
        endDate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                handleDateDialog(recurring, false, endDate, end);
                return false;
            }
        });
    }

    protected void hideForReminder(final PreferenceFragment fragment, final boolean forDue,
                                   final Preference recurring_minute, final Preference recurring_hour) {
        final PreferenceCategory cat = (PreferenceCategory) fragment.findPreference("recurring_interval");
        if (forDue) {
            cat.removePreference(recurring_hour);
            cat.removePreference(recurring_minute);
        } else {
            cat.addPreference(recurring_minute);
            cat.addPreference(recurring_hour);
        }
    }


    protected void setSummary(final NumPickerPref pref, final int id,
                              final int val) {
        String summary = getResources().getQuantityString(id,
                         val, val);
        if (val == 0) {
            summary = getString(R.string.nothing);
        }
        pref.setSummary(summary);
    }


    @SuppressLint("NewApi")
    protected void handleDateDialog(final Recurring recurring, final boolean start,
                                    final Preference date,
                                    final String s) {
        Calendar c = new GregorianCalendar();
        final String no = getString(R.string.no_begin_end, s);
        if (start && recurring.getStartDate().isPresent()) {
            c = recurring.getStartDate().get();
        } else if (start && recurring.getEndDate().isPresent()) {
            c = recurring.getEndDate().get();
        }
        final DatePickerDialog.OnDateSetListener listener = null;
        final DatePickerDialog picker = new DatePickerDialog(this,
                listener, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));
        picker.getDatePicker().setCalendarViewShown(false);
        picker.setButton(DialogInterface.BUTTON_POSITIVE,
                         getString(android.R.string.ok),
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog,
                                final int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    final DatePicker dp = picker.getDatePicker();
                    final Optional<Calendar> c = Optional.of((Calendar) new GregorianCalendar(dp
                                                 .getYear(), dp.getMonth(), dp
                                                 .getDayOfMonth()));
                    if (start) {
                        recurring
                        .setStartDate(c);
                    } else if (!recurring
                               .getStartDate().isPresent()
                               || recurring
                               .getStartDate().get().before(c)) {
                        recurring
                        .setEndDate(c);
                    } else {
                        recurring.save();
                        return;
                    }
                    date.setSummary(DateTimeHelper
                                    .formatDate(
                                        c.get(),
                                        getString(R.string.humanDateTimeFormat)));
                    recurring.save();
                }
            }
        });
        picker.setButton(DialogInterface.BUTTON_NEGATIVE, no,
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog,
                                final int which) {
                if (start) {
                    recurring.setStartDate(Optional.<Calendar>absent());
                } else {
                    recurring.setEndDate(Optional.<Calendar>absent());
                }
                date.setSummary(getString(R.string.no_begin_end, s));
                recurring.save();
            }
        });
        picker.show();
    }

}