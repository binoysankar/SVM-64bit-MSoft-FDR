package org.wipro.svm.service.impl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.swing.ImageIcon;

//public class FaceRecognitionServiceImpl implements FaceRecognitionService {
public class FaceRecognitionServiceImpl {

	// The reference image "signature" (25 representative pixels, each in R,G,B)
	// and instances of Color to make things simpler.
	private Color[][] signature;

	private static final int baseSize = 300;

	/*
	 * This method which start the image processing
	 * task.
	 */
	/* (non-Javadoc)
	 * @see org.wipro.svm.service.impl.FaceRecognitionService#processImage(java.io.File)
	 */
	public String processImage(File reference, Properties properties) throws IOException {
		String imageName = "";
		String imageDistance = "";
		
		// Put the reference, scaled, in the left part of the UI.
		RenderedImage ref = rescale(ImageIO.read(reference));

		// Calculate the signature vector for the reference.
		signature = calcSignature(ref);

		// Now we need a component to store X images in a stack, where X is the
		// number of images in the same directory as the original one.
		File[] others = getOtherImageFiles(properties);
		
		if (others.length <= 0) {
			return imageName;
		} else {
			// For each image, calculate its signature and its distance from the
			// reference signature.
			RenderedImage[] rothers = new RenderedImage[others.length];
			double[] distances = new double[others.length];
			for (int o = 0; o < others.length; o++) {
				rothers[o] = rescale(ImageIO.read(others[o]));
				distances[o] = calcDistance(rothers[o]);
			}

			// Sort those vectors *together*.
			for (int p1 = 0; p1 < others.length - 1; p1++)
				for (int p2 = p1 + 1; p2 < others.length; p2++) {
					if (distances[p1] > distances[p2]) {
						double tempDist = distances[p1];
						distances[p1] = distances[p2];
						distances[p2] = tempDist;
						RenderedImage tempR = rothers[p1];
						rothers[p1] = rothers[p2];
						rothers[p2] = tempR;
						File tempF = others[p1];
						others[p1] = others[p2];
						others[p2] = tempF;
					}
				}

			List<String> imageDistanceList = new ArrayList<String>();

			// Print and return the distances between images in DB.
			for (int o = 0; o < others.length; o++) {
				imageDistanceList.add(others[o].getName().trim() + "," + String.format("% 13.3f", distances[o]).trim());
				// Restrict ArrayList to add first match
				break;
			}

			if (imageDistanceList != null || !imageDistanceList.isEmpty() || !imageDistanceList.equals("")) {
				for (String imageDistanceStr : imageDistanceList) {
					imageDistance = imageDistanceStr;
					System.out.println(imageDistance);
				}
			}

			String[] imageAndDistance = imageDistance.split(",");
			double distance = Double.parseDouble(imageAndDistance[1]);
			System.out.println("Image Distance: " + distance);

			if (distance <= Double.parseDouble(properties.getProperty("image.difference.value"))) {
				// String[] removeFileName = imageAndDistance[0].split("\\.");
				// imageName = removeFileName[0];
				imageName = "John";
			}
		}
		
		return imageName;
	}

//	public void returnToFaceDetector(File[] others, double[] distances) {
//		getImageDistance(others, distances);
//	}

	public static double imageDistance(Image i1, Image i2) {
		BufferedImage img1 = toBufferedImage(i1);
		BufferedImage img2 = toBufferedImage(i2);
		Color[][] signature = calcSignature(rescale(img1));
		Color[][] sigOther = calcSignature(rescale(img2));
		double dist = 0;
		for (int x = 0; x < 5; x++)
			for (int y = 0; y < 5; y++) {
				int r1 = signature[x][y].getRed();
				int g1 = signature[x][y].getGreen();
				int b1 = signature[x][y].getBlue();
				int r2 = sigOther[x][y].getRed();
				int g2 = sigOther[x][y].getGreen();
				int b2 = sigOther[x][y].getBlue();
				double tempDist = Math.sqrt((r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2));
				dist += tempDist;
			}
		return dist;
	}

	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}
		image = new ImageIcon(image).getImage();
		boolean hasAlpha = hasAlpha(image);
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			int transparency = Transparency.OPAQUE;
			if (hasAlpha == true) {
				transparency = Transparency.BITMASK;
			}
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
		} catch (HeadlessException e) {
		}
		if (bimage == null) {
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha == true) {
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
		}
		Graphics g = bimage.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return bimage;
	}

	public static boolean hasAlpha(Image image) {
		if (image instanceof BufferedImage) {
			return ((BufferedImage) image).getColorModel().hasAlpha();
		}
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
		}
		return pg.getColorModel().hasAlpha();
	}

	/*
	 * This method rescales an image to 300,300 pixels using the JAI scale
	 * operator.
	 */
	private static BufferedImage rescale(BufferedImage i) {
		BufferedImage scaledImage = new BufferedImage(baseSize, baseSize, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2D = scaledImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(i, 0, 0, baseSize, baseSize, null);
		graphics2D.dispose();
		return scaledImage;
	}

	/*
	 * This method calculates and returns signature vectors for the input image.
	 */
	private static Color[][] calcSignature(RenderedImage i) {
		// Get memory for the signature.
		Color[][] sig = new Color[5][5];
		// For each of the 25 signature values average the pixels around it.
		// Note that the coordinate of the central pixel is in proportions.
		float[] prop = new float[] { 1f / 10f, 3f / 10f, 5f / 10f, 7f / 10f, 9f / 10f };
		for (int x = 0; x < 5; x++)
			for (int y = 0; y < 5; y++)
				sig[x][y] = averageAround(i, prop[x], prop[y]);
		return sig;
	}

	/*
	 * This method averages the pixel values around a central point and return
	 * the average as an instance of Color. The point coordinates are
	 * proportional to the image.
	 */
	private static Color averageAround(RenderedImage i, double px, double py) {
		// Get an iterator for the image.
		RandomIter iterator = RandomIterFactory.create(i, null);
		// Get memory for a pixel and for the accumulator.
		double[] pixel = new double[3];
		double[] accum = new double[3];
		// The size of the sampling area.
		int sampleSize = 15;
		int numPixels = 0;
		// Sample the pixels.
		for (double x = px * baseSize - sampleSize; x < px * baseSize + sampleSize; x++) {
			for (double y = py * baseSize - sampleSize; y < py * baseSize + sampleSize; y++) {
				iterator.getPixel((int) x, (int) y, pixel);
				accum[0] += pixel[0];
				accum[1] += pixel[1];
				accum[2] += pixel[2];
				numPixels++;
			}
		}
		// Average the accumulated values.
		accum[0] /= numPixels;
		accum[1] /= numPixels;
		accum[2] /= numPixels;
		return new Color((int) accum[0], (int) accum[1], (int) accum[2]);
	}

	/*
	 * This method calculates the distance between the signatures of an image
	 * and the reference one. The signatures for the image passed as the
	 * parameter are calculated inside the method.
	 */
	private double calcDistance(RenderedImage other) {
		// Calculate the signature for that image.
		Color[][] sigOther = calcSignature(other);
		// There are several ways to calculate distances between two vectors,
		// we will calculate the sum of the distances between the RGB values of
		// pixels in the same positions.
		double dist = 0;
		for (int x = 0; x < 5; x++)
			for (int y = 0; y < 5; y++) {
				int r1 = signature[x][y].getRed();
				int g1 = signature[x][y].getGreen();
				int b1 = signature[x][y].getBlue();
				int r2 = sigOther[x][y].getRed();
				int g2 = sigOther[x][y].getGreen();
				int b2 = sigOther[x][y].getBlue();
				double tempDist = Math.sqrt((r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2));
				dist += tempDist;
			}
		return dist;
	}

	/*
	 * This method get all image files from probes directory as the reference.
	 * Just for kicks include also the reference image.
	 */
	private File[] getOtherImageFiles(Properties properties) {
		File dir = new File(properties.getProperty("destination.face.images.path"));
		
		// List all the image files in that directory.
		File[] others = dir.listFiles();
		return others;
	}

}
