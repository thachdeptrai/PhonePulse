package com.phoneapp.phonepulse.di;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.phoneapp.phonepulse.data.network.AuthInterceptor;
import com.phoneapp.phonepulse.utils.Constants;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {


    @Singleton
    @Provides
    public static HttpLoggingInterceptor provideLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);  // In toàn bộ body của request/response để debug
        return interceptor;
    }

    @Singleton
    @Provides
    public static OkHttpClient provideOkHttpClient(
            AuthInterceptor authInterceptor,
            HttpLoggingInterceptor loggingInterceptor
    ) {
        return new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)                    // Thêm AuthInterceptor vào OkHttp
                .addInterceptor(loggingInterceptor)                 // Thêm LoggingInterceptor
                .connectTimeout(30, TimeUnit.SECONDS)       // Thời gian chờ kết nối tối đa 30 giây
                .readTimeout(30, TimeUnit.SECONDS)          // Thời gian đọc dữ liệu tối đa 30 giây
                .build();
    }

    @Singleton
    @Provides
    public static Gson provideGson() {
        return new GsonBuilder().setLenient().create();
    }

    @Singleton
    @Provides
    public static Retrofit provideRetrofit(Gson gson, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))         // Sử dụng Gson để parse JSON
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())      // Hỗ trợ RxJava
                .client(okHttpClient)                                           // Thiết lập client đã cấu hình (gồm interceptors)
                .build();
    }

}
