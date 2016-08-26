package org.wipro.svm.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropertyFileLoader {

  public Properties loadPropertyFile() {
    Properties props = new Properties();
    try {
      File propertyFilePath = new File("C:/Vendron/SVM/svm.properties");
//      System.out.println("!!!!!!!Access to svm.properties file path successful!!!!!");
      props.load(new FileInputStream(propertyFilePath));
//      System.out.println("!!!!!!!svm.properties loaded successfully!!!!!");
    } catch (FileNotFoundException e) {
      System.out.println("Couldn't access svm.properties file path");
      e.printStackTrace();
    } catch (IOException e) {
      System.out.println("Couldn't load svm.properties");
      e.printStackTrace();
    }

    return props;
  }
}
