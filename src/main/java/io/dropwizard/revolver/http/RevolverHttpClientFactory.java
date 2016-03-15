package io.dropwizard.revolver.http;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.dropwizard.revolver.http.auth.BasicAuthConfig;
import io.dropwizard.revolver.http.config.RevolverHttpServiceConfig;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author phaneesh
 */
@Slf4j
public class RevolverHttpClientFactory {

    public static OkHttpClient buildClient(final RevolverHttpServiceConfig serviceConfiguration) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, UnrecoverableKeyException {
        Preconditions.checkNotNull(serviceConfiguration);
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if(serviceConfiguration.isAuthEnabled()) {
            switch(serviceConfiguration.getAuth().getType().toLowerCase()) {
                case "basic":
                    val basicAuthConfig = (BasicAuthConfig) serviceConfiguration.getAuth();
                    if (!Strings.isNullOrEmpty(basicAuthConfig.getUsername())) {
                        throw new RuntimeException(String.format("No valid authentication data for service %s", serviceConfiguration.getAuth().getType()));
                    }
                    builder.authenticator((route, response) -> {
                        String credentials = Credentials.basic(basicAuthConfig.getUsername(), basicAuthConfig.getPassword());
                        return response.request().newBuilder()
                                .addHeader("Authorization", credentials)
                                .build();
                    });
                    break;
                default:
                    throw new RuntimeException(String.format("Authentication type %s is not supported", serviceConfiguration.getAuth().getType()));
            }
        }
        if(serviceConfiguration.isSecured()) {
            final String keystorePath = serviceConfiguration.getKeyStorePath();
            final String keystorePassword = (serviceConfiguration.getKeystorePassword() == null) ? "" : serviceConfiguration.getKeystorePassword();
            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                    .tlsVersions(TlsVersion.TLS_1_1)
                    .cipherSuites(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                    .build();
            builder.connectionSpecs(Collections.singletonList(spec));
            if (!StringUtils.isBlank(keystorePath)) {
                 builder.socketFactory(getSSLContext(keystorePath, keystorePassword).getSocketFactory());
            } else {
                builder.socketFactory(getSSLContext().getSocketFactory());
            }
        }
        if(serviceConfiguration.getConnectionKeepAliveInMillis() <= 0 ) {
            builder.connectionPool(new ConnectionPool(serviceConfiguration.getConnectionPoolSize(), 5, TimeUnit.MINUTES));
        } else {
            builder.connectionPool(new ConnectionPool(serviceConfiguration.getConnectionPoolSize(), serviceConfiguration.getConnectionKeepAliveInMillis(), TimeUnit.MILLISECONDS));
        }
        builder.connectTimeout(serviceConfiguration.getRuntime().getThreadPool().getTimeout(), TimeUnit.MILLISECONDS);
        return builder.build();
    }

    private static SSLContext getSSLContext(final String keyStorePath, final String keyStorePassword) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, KeyManagementException, UnrecoverableKeyException {
        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream instream = RevolverHttpClientFactory.class.getClassLoader().getResourceAsStream(keyStorePath)) {
            keyStore.load(instream, keyStorePassword.toCharArray());
        }
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(),trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext;
    }

    private static SSLContext getSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        final SSLContext sslContext = SSLContext.getInstance("TLS");
        final TrustManager[] trustManagers = new TrustManager[]{new TrustEveryoneManager()};
        sslContext.init(null, trustManagers, null);
        return sslContext;
    }

    static class TrustEveryoneManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(final java.security.cert.X509Certificate[] chain, final String authType) throws CertificateException { }

        @Override
        public void checkServerTrusted(final java.security.cert.X509Certificate[] chain, final String authType) throws CertificateException { }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

}
