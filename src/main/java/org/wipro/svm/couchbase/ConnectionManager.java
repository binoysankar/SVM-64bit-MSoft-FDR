package org.wipro.svm.couchbase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.couchbase.client.CouchbaseClient;

public class ConnectionManager {
	private CouchbaseClient client;
	
	public CouchbaseClient getClient() {
		return client;
	}
	
	public ConnectionManager() {
		try {
			Properties prop = new Properties();
			
//			File jarPath=new File(ConnectionManager.class.getProtectionDomain().getCodeSource().getLocation().getPath());
//	        String propertiesPath=jarPath.getParentFile().getAbsolutePath();
//	        prop.load(new FileInputStream(propertiesPath+"/svm.properties"));
			
			File jarPath=new File("C:/Vendron/SVM/svm.properties");
	        String propertiesPath=jarPath.getParentFile().getAbsolutePath();
	        prop.load(new FileInputStream(propertiesPath+"/svm.properties"));
			
			URI hostOne = new URI(prop.getProperty("couchbase.host.one"));
			List<URI> baseURIs = new ArrayList<URI>();
	        baseURIs.add(hostOne);

	        // Name of the Bucket to connect to
			String bucket = prop.getProperty("couchbase.svm.bucket");
	
			// Password of the bucket (empty) string if none
			String password = prop.getProperty("couchbase.bucket.password");
	
			// Connect to the Cluster
			client = new CouchbaseClient(baseURIs, bucket, password);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public void destroy() {
		if (client != null) {
			client.shutdown();
			client = null;
		}
	}

}
