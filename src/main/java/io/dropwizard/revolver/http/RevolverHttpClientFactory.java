/*
 * Copyright 2016 Phaneesh Nagaraja <phaneesh.n@gmail.com>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.dropwizard.revolver.http;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.dropwizard.revolver.http.auth.BasicAuthConfig;
import io.dropwizard.revolver.http.auth.TokenAuthConfig;
import io.dropwizard.revolver.http.config.RevolverHttpServiceConfig;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.internal.tls.OkHostnameVerifier;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.*;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author phaneesh
 */
@Slf4j
public class RevolverHttpClientFactory {

    private static LoadingCache<RevolverHttpServiceConfig, OkHttpClient> clientCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<RevolverHttpServiceConfig, OkHttpClient>() {
                @Override
                public OkHttpClient load(RevolverHttpServiceConfig serviceConfiguration) throws Exception {
                    return getOkHttpClient(serviceConfiguration);
                }
            });

    public static OkHttpClient buildClient(final RevolverHttpServiceConfig serviceConfiguration) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, UnrecoverableKeyException, ExecutionException {
        Preconditions.checkNotNull(serviceConfiguration);
        return clientCache.get(serviceConfiguration);
    }

    private static OkHttpClient getOkHttpClient(RevolverHttpServiceConfig serviceConfiguration) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, KeyManagementException, UnrecoverableKeyException {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (serviceConfiguration.isAuthEnabled()) {
            switch (serviceConfiguration.getAuth().getType().toLowerCase()) {
                case "basic":
                    val basicAuthConfig = (BasicAuthConfig) serviceConfiguration.getAuth();
                    if (!Strings.isNullOrEmpty(basicAuthConfig.getUsername())) {
                        throw new RuntimeException(String.format("No valid authentication data for service %s", serviceConfiguration.getAuth().getType()));
                    }
                    builder.authenticator((route, response) -> {
                        String credentials = Credentials.basic(basicAuthConfig.getUsername(), basicAuthConfig.getPassword());
                        return response.request().newBuilder()
                                .addHeader(HttpHeaders.AUTHORIZATION, credentials)
                                .build();
                    });
                    break;
                case "token":
                    val tokenAuthConfig = (TokenAuthConfig) serviceConfiguration.getAuth();
                    if (Strings.isNullOrEmpty(tokenAuthConfig.getPrefix())) { //No prefix check
                        builder.authenticator((route, response) -> response.request().newBuilder()
                                .addHeader(HttpHeaders.AUTHORIZATION, tokenAuthConfig.getToken())
                                .build());
                    } else { //with configured prefix
                        builder.authenticator((route, response) -> response.request().newBuilder()
                                .addHeader(HttpHeaders.AUTHORIZATION, String.format("%s %s", tokenAuthConfig.getPrefix(), tokenAuthConfig.getToken()))
                                .build());
                    }
                    break;
                default:
                    throw new RuntimeException(String.format("Authentication type %s is not supported", serviceConfiguration.getAuth().getType()));
            }
        }
        if (serviceConfiguration.isSecured()) {
            final ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .allEnabledTlsVersions()
                    .allEnabledCipherSuites()
                    .build();
            builder.connectionSpecs(Collections.singletonList(spec));
            final String keystorePath = serviceConfiguration.getKeyStorePath();
            final String keystorePassword = (serviceConfiguration.getKeystorePassword() == null) ? "" : serviceConfiguration.getKeystorePassword();
            if (!StringUtils.isBlank(keystorePath)) {
                configureSSL(keystorePath, keystorePassword, builder);
            } else {
                HostnameVerifier hostNameVerifier = (s, sslSession) -> true;
                builder.hostnameVerifier(hostNameVerifier);
            }
        }
        if (serviceConfiguration.getConnectionKeepAliveInMillis() <= 0) {
            builder.connectionPool(new ConnectionPool(serviceConfiguration.getConnectionPoolSize(), 30, TimeUnit.SECONDS));
        } else {
            builder.connectionPool(new ConnectionPool(serviceConfiguration.getConnectionPoolSize(), serviceConfiguration.getConnectionKeepAliveInMillis(), TimeUnit.MILLISECONDS));
        }
        builder.retryOnConnectionFailure(true);
        builder.connectTimeout(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        builder.readTimeout(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        builder.writeTimeout(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        builder.connectTimeout(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        builder.followRedirects(false);
        builder.followSslRedirects(false);
        return builder.build();
    }

    private static void configureSSL(final String keyStorePath, final String keyStorePassword, OkHttpClient.Builder clientBuilder) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, KeyManagementException, UnrecoverableKeyException {
        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream instream = RevolverHttpClientFactory.class.getClassLoader().getResourceAsStream(keyStorePath)) {
            keyStore.load(instream, keyStorePassword.toCharArray());
        }
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
        clientBuilder.hostnameVerifier(OkHostnameVerifier.INSTANCE);
        clientBuilder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager)trustManagerFactory.getTrustManagers()[0]);
    }

}
