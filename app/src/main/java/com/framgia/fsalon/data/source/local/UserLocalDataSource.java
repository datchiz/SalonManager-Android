package com.framgia.fsalon.data.source.local;

import com.framgia.fsalon.data.model.UserRespone;
import com.framgia.fsalon.data.source.UserDataSource;
import com.framgia.fsalon.data.source.local.sharepref.SharePreferenceApi;
import com.google.gson.Gson;

import io.reactivex.Observable;

import static com.framgia.fsalon.data.source.local.sharepref.SharePreferenceKey.PREF_PHONE;
import static com.framgia.fsalon.data.source.local.sharepref.SharePreferenceKey.PREF_USER;

/**
 * Created by framgia on 7/20/17.
 */
public class UserLocalDataSource implements UserDataSource.LocalDataSource {
    private SharePreferenceApi mSharePreference;

    public UserLocalDataSource(SharePreferenceApi sharePreference) {
        mSharePreference = sharePreference;
    }

    @Override
    public Observable<UserRespone> getCurrentUser() {
        String json = mSharePreference.get(PREF_USER, String.class);
        UserRespone userRespone = new Gson().fromJson(json, UserRespone.class);
        if (userRespone == null) {
            return Observable.error(new NullPointerException());
        } else {
            return Observable.just(userRespone);
        }
    }

    @Override
    public Observable<Boolean> saveCurrentUser(UserRespone userRespone) {
        return Observable.just(mSharePreference.put(PREF_USER, new Gson().toJson(userRespone)));
    }

    @Override
    public void clearCurrentUser() {
        mSharePreference.remove(PREF_USER);
        mSharePreference.remove(PREF_PHONE);
    }

    public Observable<Boolean> saveCurrentPhone(String phone) {
        return Observable.just(mSharePreference.put(PREF_PHONE, phone));
    }

    @Override
    public Observable<String> getCurrentPhone() {
        String phone = mSharePreference.get(PREF_PHONE, String.class);
        if (phone == null) {
            return Observable.error(new NullPointerException());
        } else {
            return Observable.just(phone);
        }
    }
}
