import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

public class mainRunner {
/** ================================================================================
 * Data Compression - Final Project
 * 
 * Compresses video using "RLE" for images - only the first frame and 
 * frames that contain movement are encoded (using a PNG encoder implemented by us), 
 * so that the compressed file comprises of:(<imgArrSize(int)><imgDataArr(byte[])><timesToShow(int)>)* # of frames with movement.
 * 
 * PNG encoder implemented based on libPNG specs (http://www.libpng.org/pub/png/spec/1.2/PNG-Contents.html)
 * Movement detection is done using OpenCV for Java (https://opencv.org/releases.html).
 * ...............................................
 * Submitted by: 
 * @author Dor Avitan ; @author Omer Sirpad 
 * ================================================================================
 */
	public static void main(String[] args) {
		long sTime = System.currentTimeMillis();
		String fileName = "horse.mp4";
		MotionDetector detector = new MotionDetector(fileName, "", "D:",5);
		//detector.motionFinder();
/*		BufferedImage picture=null;
		try {
			 picture = ImageIO.read(new File("1.jpg"));
		} catch (IOException e) {
			System.out.println("Error reading file");
			e.printStackTrace();
		}
		detector.roiExtractor(picture);
*/		long mTime = System.currentTimeMillis();
		System.out.println("Analyzed  in: " + (double) (mTime - sTime) / 1000 + " s");

	}

}
