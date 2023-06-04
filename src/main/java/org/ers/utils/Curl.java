package org.ers.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

/**
 * @Author Ahmed Ali
 * @Email a.ali.tsc@gmail.com
 */
public final class Curl {

    private static final Logger log = LoggerFactory.getLogger(Curl.class);

    private Curl() {}

    public static Executor executor(String workingDirectory) {
        Objects.requireNonNull(workingDirectory, "working directory can't be null");
        ProcessBuilder processBuilder = new ProcessBuilder("curl", "-X");
        log.info("Setting working directory for CURL: [{}]", workingDirectory);
        processBuilder.directory(new File(workingDirectory));
        return new Executor(processBuilder);
    }

    public static Executor executor() {
        ProcessBuilder processBuilder = new ProcessBuilder("curl", "-X");
        return new Executor(processBuilder);
    }

    public static class Executor {
        private ProcessBuilder processBuilder;
        private boolean enableLogs;

        private Executor(ProcessBuilder processBuilder) {
            this.processBuilder = processBuilder;
        }

        public Executor post(String endpoint) {
            if(isNotEmpty(endpoint)) {
                this.processBuilder.command().add("POST");
                processBuilder.command().add(endpoint);
            }
            return this;
        }

        public Executor get(String endpoint) {
            if(isNotEmpty(endpoint)) {
                this.processBuilder.command().add("GET");
                processBuilder.command().add(endpoint);
            }
            return this;
        }

        public Executor header(String key, String value) {
            if(isNotEmpty(key) && isNotEmpty(value)) {
                processBuilder.command().add("--header");
                processBuilder.command().add(key + ": " + value);
            }
            return this;
        }

        public Executor formDate(String key, String value) {
            if(isNotEmpty(key) && isNotEmpty(value)) {
                processBuilder.command().add("--data-urlencode");
                processBuilder.command().add(key + "=" + value);
            }
            return this;
        }

        private Executor key(String key) {
            if(isNotEmpty(key)) {
                processBuilder.command().add("--key");
                processBuilder.command().add(key);
            }
            return this;
        }

        private Executor cert(String cert) {
            if(isNotEmpty(cert)) {
                processBuilder.command().add("--cert");
                processBuilder.command().add(cert);
            }
            return this;
        }

        public Executor trustSelfSignedCertificate(boolean trust) {
            if(trust)
                processBuilder.command().add("-k");
            return this;
        }

        public Executor contentType(String contentType) {
            if (isNotEmpty(contentType)) {
                processBuilder.command().add("--header");
                processBuilder.command().add("Content-Type: ".concat(contentType.toString()));
            }
            return this;
        }

        public Executor basicAuth(String username, String password) {
            if(isNotEmpty(username) && isNotEmpty(password)) {
                processBuilder.command().add("--header");
                processBuilder.command().add("Authorization: ".concat(createBasicAuthHeader(username, password)));
            }
            return this;
        }

        private String createBasicAuthHeader(String username, String password) {
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
            String authHeader = "Basic " + new String(encodedAuth);
            return authHeader;
        }

        public Executor bearerAuth(String token) {
            if(isNotEmpty(token)) {
                processBuilder.command().add("--header");
                processBuilder.command().add("Authorization: Bearer ".concat(token));
            }
            return this;
        }

        public Executor mTLS(String keyPath, String certificatePath) {
            this.key(keyPath);
            this.cert(certificatePath);
            return this;
        }

        public Executor enableLogs(boolean enableLogs) {
            this.enableLogs = enableLogs;
            return this;
        }

        public Executor jsonData(String json) {
            if(isNotEmpty(json)) {

                if(this.enableLogs)
                    log.info("Request [{}]", json);

                this.contentType("application/json");
                processBuilder.command().add("-d");
                processBuilder.command().add(json);
            }
            return this;
        }

        public Executor pojoToJson(Object object) {
            if(isNotEmpty(object)) {
                String json = JsonUtils.toJson(object);
                if(this.enableLogs)
                    log.info("Request [{}]", json);

                this.contentType("application/json");
                processBuilder.command().add("-d");
                processBuilder.command().add(json);
            }
            return this;
        }

        public Executor formDate(Map<String, String> formData) {
            if(isNotEmpty(formData)) {
                formData.forEach(this::formDate);
            }
            return this;
        }

        public Executor headers(Map<String, String> httpHeaders) {
            if(isNotEmpty(httpHeaders)) {
                httpHeaders.forEach(this::header);
            }
            return this;
        }

        public <T> T execute(Class<T> clazz) throws IOException {
            if(isEmpty(processBuilder.command()))
                throw new RuntimeException("No Commands to execute");

            Process process = processBuilder.start();

            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            if(enableLogs) {
                System.out.println(processBuilder.command().toString());
                log.info("Response: {}", output);
            }

            if(clazz.getName().equals(String.class.getName()))
                return (T) output;

            return new ObjectMapper().readValue(output, clazz);
        }

        public void execute() throws IOException {
            if(isEmpty(processBuilder.command()))
                throw new RuntimeException("No Commands to execute");

            Process process = processBuilder.start();

            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            if(enableLogs) {
                System.out.println(processBuilder.command().toString());
                log.info("Response: {}", output);
            }
        }
    }
}

