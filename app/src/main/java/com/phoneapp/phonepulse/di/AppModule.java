package com.phoneapp.phonepulse.di;

import android.content.Context;
import android.content.SharedPreferences;

import com.phoneapp.phonepulse.utils.Constants;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {
    @Provides
    @Singleton
    public static SharedPreferences provideSharedPreferences(@ApplicationContext Context context) {
        return context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
    }
}