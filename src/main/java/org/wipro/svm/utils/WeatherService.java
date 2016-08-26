package org.wipro.svm.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wipro.svm.model.Weather;

public class WeatherService {
	
	private static Properties props;

	public WeatherService(Properties props) {
		WeatherService.props = props;
	}
	
	public Weather doQuery(String subUrl) {
		Weather weather = null;
		String responseBody = null;
		HttpGet httpGet = null;
		try {
			HttpHost proxy = new HttpHost(props.getProperty("http.proxyHost"),
					Integer.parseInt(props.getProperty("http.proxyPort")));

			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(
					new AuthScope(props.getProperty("http.proxyHost"),
							Integer.parseInt(props.getProperty("http.proxyPort"))),
					new UsernamePasswordCredentials(props.getProperty("http.proxyUsername"),
							props.getProperty("http.proxyPassword")));
			CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

			httpGet = new HttpGet(SvmConstants.WEATHER_BASE_URL + subUrl);
			
			if (props.getProperty("app.id") != null) {
				httpGet.addHeader(SvmConstants.APPID_HEADER, props.getProperty("app.id"));
			}
			RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
			httpGet.setConfig(config);

			CloseableHttpResponse response = httpClient.execute(proxy, httpGet);
			///////////////////////////////// ANOTHER WAY OF
			///////////////////////////////// IMPLEMENTATION///////////////////////////////////////////////////////
			// DefaultHttpClient httpclient = new DefaultHttpClient();
			// httpclient.getCredentialsProvider().setCredentials(
			// new AuthScope("proxy4.wipro.com", 8080),
			// new UsernamePasswordCredentials("BI300892", "chelsea@444"));
			//
			// this.httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
			///////////////////////////////// proxy);
			// HttpResponse response = this.httpClient.execute(proxy, httpget);
			/////////////////////////////// ANOTHER WAY OF
			///////////////////////////////// IMPLEMENTATION///////////////////////////////////////////////////////
			
			/* Read the response content */
			responseBody = EntityUtils.toString(response.getEntity());
			
			weather = parseJSONResponse(new JSONObject(responseBody), weather);
		} catch (IOException e) {
			return weather;
		} catch (RuntimeException re) {
			httpGet.abort();
			return weather;
		} catch (Exception ex) {
			return weather;
		} 
		
		return weather;
	}
	
	private static Weather parseJSONResponse(JSONObject response, Weather weather) throws JSONException {
		weather = new Weather();
		StringBuffer conditionBuf = new StringBuffer();
		
		weather.setCity(response.getString(SvmConstants.NAME));
		weather.setCountry(response.getJSONObject(SvmConstants.SYS).getString(SvmConstants.COUNTRY));
		JSONArray condition = response.getJSONArray(SvmConstants.WEATHER);
		for(int i = 0 ; i < condition.length() ; i++){
//			weather.setCondition(conditionBuf.append(condition.getJSONObject(i).getString("main")).append(" - ")
//					.append(condition.getJSONObject(i).getString("description")).toString());
			weather.setCondition(conditionBuf.append(condition.getJSONObject(i).getString(SvmConstants.MAIN)).toString());
		}
		weather.setTemp(new Double(response.getJSONObject(SvmConstants.MAIN).getString(SvmConstants.TEMPERATURE)).intValue());
		weather.setHumidity(new Double(response.getJSONObject(SvmConstants.MAIN).getString(SvmConstants.HUMIDITY)).intValue());
//		weather.setPressure(new Double(response.getJSONObject(SvmConstants.MAIN).getString(SvmConstants.PRESSURE)).intValue());
//		weather.setChill(response.getJSONObject(SvmConstants.WIND).getString(SvmConstants.DEGREES));
		
		return weather;
	}

}
