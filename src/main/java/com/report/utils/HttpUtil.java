package com.report.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 网路工具类
 * @author Charles Wesley
 * @date 2019/11/30 16:52
 */
@SuppressWarnings("Duplicates")
public class HttpUtil {

    public static String doPost(String url, Map<String, String> paramMap, Map<String, String> headerMap){
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        //构建参数
        StringBuffer params = new StringBuffer();
        if(paramMap != null){
            paramMap.forEach((key, value) -> {
                try {
                    if(key == null){
                        params.append(URLEncoder.encode(value, "UTF-8"));
                    }else{
                        params.append(key).append("=").append(URLEncoder.encode(value, "UTF-8"));
                    }
                    params.append("&");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            });
            params.deleteCharAt(params.length()-1);
            url += "?" + params.toString();
        }

        //创建Post请求
        HttpPost httpPost = new HttpPost(url);
        if(headerMap != null){
            headerMap.forEach(httpPost::setHeader);
        }

        //响应模型
        CloseableHttpResponse response = null;
        try {
            //从响应模型中获得应答实体
            response = httpClient.execute(httpPost);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Map<String, Object> doGet(String url, Map<String, String> paramMap, Map<String, String> headerMap){
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        //构建参数
        StringBuffer params = new StringBuffer();
        if(paramMap != null){
            paramMap.forEach((key, value) -> {
                try {
                    if(key == null){
                        params.append(URLEncoder.encode(value, "UTF-8"));
                    }else{
                        params.append(key).append("=").append(URLEncoder.encode(value, "UTF-8"));
                    }
                    params.append("&");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            });
            params.deleteCharAt(params.length()-1);
            url += "?" + params.toString();
        }

        //创建Get请求
        HttpGet httpGet = new HttpGet(url);
        if(headerMap != null){
            headerMap.forEach(httpGet::setHeader);
        }
        //响应模型
        CloseableHttpResponse response = null;
        try {
            //从响应模型中获得应答实体
            response = httpClient.execute(httpGet);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("body", EntityUtils.toString(response.getEntity()));
            resultMap.put("header", response.getAllHeaders());
            return resultMap;
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static byte[] doGetBytes(String url, Map<String, String> paramMap, Map<String, String> headerMap) throws Exception{
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        //构建参数
        StringBuffer params = new StringBuffer();
        if(paramMap != null){
            paramMap.forEach((key, value) -> {
                try {
                    if(key == null){
                        params.append(URLEncoder.encode(value, "UTF-8"));
                    }else{
                        params.append(key).append("=").append(URLEncoder.encode(value, "UTF-8"));
                    }
                    params.append("&");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            });
            params.deleteCharAt(params.length()-1);
            url += "?" + params.toString();
        }

        //创建Get请求
        HttpGet httpGet = new HttpGet(url);
        if(headerMap != null){
            headerMap.forEach(httpGet::setHeader);
        }
        //响应模型
        CloseableHttpResponse response = null;
        try {
            //从响应模型中获得应答实体
            response = httpClient.execute(httpGet);
            return EntityUtils.toByteArray(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
