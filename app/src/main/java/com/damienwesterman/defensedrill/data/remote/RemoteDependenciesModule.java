/****************************\
 *      ________________      *
 *     /  _             \     *
 *     \   \ |\   _  \  /     *
 *      \  / | \ / \  \/      *
 *      /  \ | / | /  /\      *
 *     /  _/ |/  \__ /  \     *
 *     \________________/     *
 *                            *
 \****************************/
/*
 * Copyright 2025 Damien Westerman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.damienwesterman.defensedrill.data.remote;

import android.content.Context;
import android.util.Log;
import android.webkit.URLUtil;

import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.utils.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Dagger module for Dependency Injection for the remote data layer.
 */
@Module
@InstallIn(SingletonComponent.class)
public class RemoteDependenciesModule {
    private static final String TAG = RemoteDependenciesModule.class.getSimpleName();

    @Provides
    // NOT Singleton, under normal use case this should be used about once per month
    /* package-private */ static AuthDao getAuthDao(@ApplicationContext Context context) {
        return new Retrofit.Builder()
                .baseUrl(getServerUrl())
                .client(getPinnedClient(context))
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthDao.class);
    }

    @Provides
    @Singleton
    public static ApiRepo getApiRepo(SharedPrefs sharedPrefs, @ApplicationContext Context context) {
        return new ApiRepo(sharedPrefs, createApiDao(context));
    }

    private static String getServerUrl() {
        String serverUrl = Constants.SERVER_URL;
        if (!URLUtil.isValidUrl(serverUrl)) {
            Log.e(TAG, "Invalid Server URL: " + serverUrl);
            throw new IllegalArgumentException("Invalid server URL: '" + serverUrl + "'");
        }

        return serverUrl;
    }

    private static ApiDao createApiDao(Context context) {
        return new Retrofit.Builder()
                .baseUrl(getServerUrl())
                .client(getPinnedClient(context))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiDao.class);
    }

    private static Certificate loadCert(Context context) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            try (InputStream caInput = context.getAssets().open("isrgrootx1.pem")) {
                return cf.generateCertificate(caInput);
            }
        } catch (CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static OkHttpClient getPinnedClient(Context context) {
        // Load the root certificate
        Certificate ca = loadCert(context);

        // Create a new KeyStore with the trusted root
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null); // Initialize empty keystore
            keyStore.setCertificateEntry("isrgrootx1", ca);
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // Create a TrustManager that trusts the CAs in our KeyStore
        TrustManagerFactory tmf = null;
        try {
            tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException(e);
        }

        // Create an SSLContext that uses our TrustManager
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }

        return new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) tmf.getTrustManagers()[0])
                .build();
    }
}
