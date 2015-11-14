package com.addondowner;

import com.google.gson.Gson;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by johlar on 30/03/15.
 *
 */
public class NetHandler {

	private static final String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:36.0) Gecko/20100101 Firefox/36.0";
	private static final String acceptMethods = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
	static final String internetCheckHost = "www.curse.com";
	static String host = "addondowner.homeip.net";

	public static String fileDownloader(String fileUrl) throws IOException {
		String fileName = getFileNameFromUrl(fileUrl);
		File destDir = new File(AddonDowner.TEMP_FILE_DIR);
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		File file = new File(AddonDowner.TEMP_FILE_DIR + fileName);
		if (file.exists()) {
			System.out.println("has file: " + fileName);
		} else {
			System.out.println("downloading: " + fileName);
			Connection.Response fileResponse = getConnection(fileUrl).maxBodySize(0).execute();
			FileOutputStream out = (new FileOutputStream(new java.io.File(AddonDowner.TEMP_FILE_DIR + fileName)));
			out.write(fileResponse.bodyAsBytes());
			out.close();
			System.out.println("file downloaded: " + fileName);
		}
		return fileName;
	}



	public static String getDataHrefFromUrl(String url) throws IOException {
		String data = "";
		Map<String, String> cookies = new HashMap<String, String>();
		Connection.Response response = null;
		String referrer = "http://www.google.com";
		while (url.length() > 1) {
			Connection connect = getConnection(url).followRedirects(false).referrer(referrer);
			if (cookies.size() > 0) {
				for (Map.Entry<String, String> entry : cookies.entrySet()) {
					connect = connect.cookie(entry.getKey(), entry.getValue());
					//System.out.println(entry.getKey() + " = " + entry.getValue());
				}
			}
			response = connect.execute();
			Map<String, String> cookies1 = response.cookies();
			for (Map.Entry<String, String> entry : cookies1.entrySet()) {
				cookies.put(entry.getKey(), entry.getValue());
				//System.out.println("Adding cookie: " + entry.getKey() + " = " + entry.getValue());
			}
			String responseLocation = response.header("Location");
			if (null != responseLocation && responseLocation.length() > 2) {
				referrer = url;
				if (responseLocation.startsWith("/")) {
					url = url.substring(0, url.indexOf("/", url.indexOf("/", url.indexOf("/") + 1) + 1)) + responseLocation;
				} else if (responseLocation.startsWith("http://") || responseLocation.startsWith("https://")) {
					url = responseLocation;
				} else {
					url = url + responseLocation;
				}
				//System.out.println("Found redirect: " + url);
			} else {
				url = "";
			}
		}

		if (null != response) {
			Document document = response.parse();
			Elements select = document.select("a[data-href]");
			if (select.size() > 0) {
				Element first = select.first();
				data = first.attr("data-href");
			} else {
				Elements links = document.select("a[href]");
				for (Element link : links) {
					String href = link.attr("href");
					if (href.contains("downloads/elvui")) {
						data = href;
						break;
					}
				}
			}
		}
		return data;
	}

	private static Connection getConnection(String url) {
		return Jsoup.connect(url).ignoreContentType(true).userAgent(userAgent).header("Accept", acceptMethods).timeout(AddonDowner.HTTP_TIMEOUT);
	}

	public static void serverUploadAddonMeta(Map<String, String> params) throws IOException {
		String url = "http://" + host + "/cgi-bin/addonmeta.cgi";
		String testParams = "";
		Connection connection = getConnection(url);
		for (Map.Entry<String, String> param : params.entrySet()) {
			connection = connection.data(param.getKey(), param.getValue());
			if(testParams.length() > 1){
				testParams += "&";
			} else {
				testParams += "?";
			}
			testParams += param.getKey() + "=" + param.getValue();
		}
		System.out.println(url + testParams);
		Document document = connection.post();
		System.out.println(document.body().toString());
	}

	public static String getFileNameFromUrl(String fileUrl) {
		return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
	}

	public static Addon[] getServerAddonList() throws IOException {
		String url = "http://" + host + "/cgi-bin/addonlist.cgi";
		Connection.Response fileResponse = getConnection(url).execute(); // todo add shorter timeout
		Gson gson = new Gson();
		return gson.fromJson(fileResponse.body(), Addon[].class);
	}

	public static Addon[] getAddonsFromDirs(String[] directories) throws IOException {

		String url = "http://" + host + "/cgi-bin/addonfromdirs.cgi";
		String[] postString = new String[directories.length * 2];
		for (int i = 0; i < directories.length; i++) {
			String directory = directories[i];
			int nr = i*2;
			postString[nr] = "dir";
			postString[nr+1] = directory;
		}
		Connection.Response fileResponse = getConnection(url).data(postString).execute();
		Gson gson = new Gson();
		return gson.fromJson(fileResponse.body(), Addon[].class);
	}

	public static boolean checkForNetwork() {
		String url = "http://" + internetCheckHost;
		boolean gotContact = false;
		int retries = 0;

		while (!gotContact && retries < 10) {
			try {
				Connection.Response response = getConnection(url).execute();
				if(response.statusCode() == 200){
					gotContact = true;

					response = getConnection("http://" + host).execute();
					if(response.statusCode() != 200){
						host = "192.168.1.100";
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				retries++;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					return false;
				}
			}
		}
		return gotContact;
	}
}
