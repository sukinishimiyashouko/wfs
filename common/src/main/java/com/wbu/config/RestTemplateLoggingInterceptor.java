package com.wbu.config;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @auther 11852
 * @create 2023/8/2
 */
@Slf4j
@Component
public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest
                                        , byte[] body
                                        , ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {

        RequestInfo requestInfo = extractRequest(httpRequest, body);
        ClientHttpResponse response = clientHttpRequestExecution.execute(httpRequest, body);

        ResponseInfo responseInfo = extractResponse(response);
        log.info("call url: {},method: {},request: {},response: {}",
                requestInfo.getUrl(),
                requestInfo.getMethod().toString(),
                requestInfo,
                responseInfo);
        return response.getStatusCode().is5xxServerError() ? null : response;

    }


    private RequestInfo extractRequest(HttpRequest httpRequest,byte[] body){
        RequestInfo requestInfo = new RequestInfo();
        return requestInfo.setUrl(httpRequest.getURI().toString())
                .setMethod(httpRequest.getMethodValue())
                .setHeaders(httpRequest.getHeaders().toString())
                .setBody(body.toString());
    }

    private ResponseInfo extractResponse(ClientHttpResponse response) throws IOException {
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.setStatusCode(response.getStatusCode().value())
                .setStatusText(response.getStatusText())
                .setHeaders(response.getHeaders().toString());

        InputStream inputStream = response.getBody();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line=bufferedReader.readLine())!=null){
            builder.append(line).append("/n");
        }
        responseInfo.setBody(builder.toString());
        return responseInfo;

    }
    /**
     * 请求url
     * 请求方法
     * 请求头header
     * 请求参数
     */
    @Data
    @Accessors(chain = true)
    public static class RequestInfo {
        private String url;
        private String method;
        private String headers;
        private String body;
    }

    /**
     * status code
     * status text
     * headers
     * response body
     */
    @Data
    @Accessors(chain = true)
    public static class ResponseInfo {
        private Integer statusCode;
        private String statusText;
        private String headers;
        private String body;
    }
}
