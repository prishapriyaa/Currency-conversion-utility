package myproject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

public class getAPI {
	public static void main(String[] args) {
		Properties prop=new Properties();
		InputStream input=new FileInputStream("../../../global.properties");
		prop.load(input);

		String urlString=prop.getProperty("api_github_url");
		//http prerequest
		
		HttpRequest request=HttpRequest.newBuilder().GET().uri(URI.create(urlString)).build();
		
		HttpClient client= HttpClient.newBuilder().build();
		HttpResponse<String> response = null;
		try {
			response= client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(response.statusCode());
		System.out.println(response.body());
	}
}
