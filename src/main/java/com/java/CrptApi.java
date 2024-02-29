package com.java;

import com.google.gson.Gson;
import lombok.*;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {
    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final AtomicInteger requestCount;
    private final Object lock = new Object();
    private final CloseableHttpClient httpClient;
    private final Gson gson;
    private final Logger logger;
    public final static String URL = "https://ismp.crpt.ru/api/v3/1k/documents/create";

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        this.requestCount = new AtomicInteger(0);
        this.httpClient = HttpClients.createDefault();
        this.gson = new Gson();
        this.logger = LoggerFactory.getLogger(CrptApi.class);
    }

    void createDocument(Document document, String signature) {
        //TODO
        synchronized (lock) {
            if (requestCount.get() >= requestLimit) {
                try {
                    lock.wait(timeUnit.toMillis(1));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            try {
                HttpPost request = new HttpPost(URL);
                StringEntity entity = new StringEntity(gson.toJson(document));
                request.setEntity(entity);
                request.setHeader("Content-Type", "application/json");
                logger.info("Документ успешно создан с подписью: {}", signature);
                requestCount.incrementAndGet();
            } catch (Exception e) {
                logger.error("Произошла ошибка при создании документа", e);
            }
        }
    }

    void getDataFromResponse(HttpPost request) {
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            StringBuilder responseContent = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                responseContent.append(line);
            }
            for (int i = 0; i < responseContent.length(); i++) {
                logger.info("Содержимое ответа от сервера: {}", responseContent);
            }
        } catch (IOException e) {
            logger.error("Ошибка при получении данных из ответа", e);
        }
    }

    void saveDocument(Document document) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("document.ser"))) {
            oos.writeObject(document);
            logger.info("Документ успешно сохранен в файл document.ser");
        } catch (IOException e) {
            logger.error("Ошибка при сохранении документа в файл", e);
        }
    }


    void checkingSuccessfulCreatedHTTPRequest(HttpPost request) {
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_CREATED) {
                logger.info("Документ успешно создан. Код состояния: " + statusCode);
            } else {
                logger.error("Ошибка при создании документа. Код состояния: " + statusCode);
            }
        } catch (IOException e) {
            logger.error("Произошла ошибка при создании документа", e);
        }
    }

    boolean checkRequestMethodAllowed(String url) {
        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpOptions request = new HttpOptions(url);
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            return statusCode == 405;
        } catch (Exception e) {
            logger.error("Сервер с адресом " + url + " недоступен!", e);
            return false;
        }
    }
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Builder
    static class Product implements Serializable {
        private String certificate_document;
        private String certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private String production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;
        private String reg_date;
        private String reg_number;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Builder
    static class Document implements Serializable {
        private String participantinn;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        private Product[] products;
    }


    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 5);

        Product product = Product.builder()
                .certificate_document("certificate_document")
                .certificate_document_date("2020-01-23")
                .certificate_document_number("certificate_document_number")
                .owner_inn("owner_inn")
                .producer_inn("producer_inn")
                .production_date("2020-01-23")
                .tnved_code("tnved_code")
                .uit_code("uit_code")
                .uitu_code("uitu_code")
                .reg_date("2020-01-23")
                .reg_number("reg_number")
                .build();

        Document document = Document.builder()
                .participantinn("1234567890")
                .doc_id("doc1")
                .doc_status("pending")
                .doc_type("LP_INTRODUCE_GOODS")
                .importRequest(true)
                .owner_inn("owner_inn")
                .participant_inn("participant_inn")
                .producer_inn("producer_inn")
                .production_date("2020-01-23")
                .production_type("production_type")
                .products(new Product[]{product})
                .build();

        String signature = "signature1";
        crptApi.createDocument(document, signature);
    }
}

