import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.ImageProducer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import com.sun.xml.internal.ws.org.objectweb.asm.Label;
/*
 * CAP_PROP_POS_MSEC Current position of the video file in milliseconds or video capture timestamp.
CAP_PROP_POS_FRAMES 0-based index of the frame to be decoded/captured next.
CAP_PROP_POS_AVI_RATIO Relative position of the video file: 0 - start of the film, 1 - end of the film.
CAP_PROP_FRAME_WIDTH Width of the frames in the video stream.
CAP_PROP_FRAME_HEIGHT Height of the frames in the video stream.
CAP_PROP_FPS Frame rate.
CAP_PROP_FOURCC 4-character code of codec.
CAP_PROP_FRAME_COUNT Number of frames in the video file.
CAP_PROP_FORMAT Format of the Mat objects returned by retrieve() .
CAP_PROP_MODE Backend-specific value indicating the current capture mode.
CAP_PROP_BRIGHTNESS Brightness of the image (only for cameras).
CAP_PROP_CONTRAST Contrast of the image (only for cameras).
CAP_PROP_SATURATION Saturation of the image (only for cameras).
CAP_PROP_HUE Hue of the image (only for cameras).
CAP_PROP_GAIN Gain of the image (only for cameras).
CAP_PROP_EXPOSURE Exposure (only for cameras).
CAP_PROP_CONVERT_RGB Boolean flags indicating whether images should be converted to RGB.
CAP_PROP_WHITE_BALANCE Currently not supported
CAP_PROP_RECTIFICATION Rectification flag for stereo cameras (note: only supported by DC1394 v 2.x backend currently)

 * */
public class MotionDetector {

	protected String fileName;
	protected String outFolder;
	protected String path;
	protected String outFileName;
	protected int minTimeBetweenKeyframes;
	protected JFrame vidFrame;
	VideoCapture vid=null;
	
	protected int sensitivity;
	protected int progress=0;

	public MotionDetector(String _file, String _path, String _outFolder, int _minTimeBetweenKeyframes) {
		String workingDir = System.getProperty("user.dir");
		String pathToOpenCVLib = workingDir + "\\openCVLib\\opencv_java342.dll";
		System.load(pathToOpenCVLib);
		fileName = _file;
		path = _path;
		outFolder = _outFolder;
		outFileName = outFolder + "\\" + fileName.substring(0, fileName.length() - 4) + "_movement.csv";
		minTimeBetweenKeyframes = _minTimeBetweenKeyframes;
		vid = new VideoCapture();
		String vidToOpen = path + "\\" + fileName;
		vid.open(vidToOpen);
		if (!vid.isOpened()) {
			System.out.println("Error opening video!");
			System.exit(1);
		}
	}
	public int getProgress(){
		return progress;
	}
	public int getLength(){
		return (int) (vid.get(7)/vid.get(5));
	}
	

	
	
	/**
	 * 
	 * Movement detection done by grayscaling & bluring the 2 images and finding
	 * differences between them. The differences are then being compared to a
	 * predefined threshold (to ignore noise).
	 * 
	 * @return name of the compressed file (name of input file with "odo"
	 *         extension).
	 */
	protected String[] motionFinder(int _startTime,int _endTime,int _sensitivity) {


		String movementGuess = null;// TODO Remove - debugging
		StringBuilder sb = null;
		sensitivity=_sensitivity;
		Mat frame = new Mat();
		Mat lastCodedFrame = new Mat();
		Mat gray = new Mat();
		Mat frameDelta = new Mat();
		Mat thresh = new Mat();
		Mat roiMat = null;
		
		
		List<MatOfPoint> contours = null;
		
		String time;
		int lastCodedFrameNum, currFrameNum,numOfFrames;
		int startTime, startFrame, endTime, endFrame;

		double timetoopenframes = 0,timetoprocessframes = 0,timetodisplayframes = 0;
		double timers,timere;
		double fps;
		boolean motion = false;

		long sTime = System.currentTimeMillis();


		
		
		startTime = _startTime;
		endTime = _endTime;
		
		numOfFrames = (int) vid.get(7);
		vidFrame=new JFrame();
		PrintWriter pw =null;
		int fileCounter=0;
		File out=null;

		do{
		try {
			if(fileCounter!=0)
				outFileName=outFileName.substring(0, outFileName.length() - 6)+"_"+Integer.toString(++fileCounter)+".csv";
			else
				fileCounter++;
			out = new File(outFileName);
			
			if(!out.exists()){
				pw = new PrintWriter(new File(outFileName));
			}
			outFileName=outFileName.substring(0, outFileName.length() - 4)+"_"+Integer.toString(++fileCounter)+".csv";
		} catch (FileNotFoundException e) {
			System.out.println("Output file path not found !");
		}
		}while(pw==null);
		pw.write("Time in Video,Description,num of contours,size of contour found\n");
		vid.read(frame);
		vidFrame.setSize(frame.width(), frame.height());
		int multiplier = 10;
		int skippedFrames = 0;

		fps = vid.get(5);
		startFrame = (int) (startTime * fps);
		endFrame = (int) (endTime * fps);
		currFrameNum = startFrame;
		Logger(1, "Analyzing video " + fileName + ". Frames: " + (endFrame-startFrame) + "from: " + numOfFrames + " with Sensitivity: " + sensitivity);
		vid.set(1, startFrame);
		
		timers=System.currentTimeMillis();
		
		Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);// convert frame
																// to grayscale
																// & save as
																// firstFrame
		Imgproc.GaussianBlur(gray, gray, new Size(21, 21), 0);// apply blur
																// (prevents
																// noise from
																// being
																// interpreted
																// as movement)
		lastCodedFrame = gray.clone();
		timere=System.currentTimeMillis();
		timetoprocessframes += timere-timers;
		lastCodedFrameNum = 0; // To avoid multiplt timestamps for same movement
		
		timers=System.currentTimeMillis();
		while (vid.read(frame) && currFrameNum < endFrame) {
			timere=System.currentTimeMillis();
			timetoopenframes+=timere-timers;
			timers=System.currentTimeMillis();
			Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
			Imgproc.GaussianBlur(gray, gray, new Size(25, 25), 0);

			Core.absdiff(lastCodedFrame, gray, frameDelta);// compute difference
															// between first
															// frame and current
															// frame
			Imgproc.threshold(frameDelta, thresh, 20, 255, Imgproc.THRESH_BINARY);
			Imgproc.dilate(thresh, thresh, new Mat(), new Point(-1, -1), 2);
			contours = new ArrayList<MatOfPoint>();
			Imgproc.findContours(thresh, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			
			timere=System.currentTimeMillis();
			timetoprocessframes+=timere-timers;
			if (currFrameNum % multiplier == 0) {
				//Logger(1, "current frame " + currFrameNum + ". Skipping " + multiplier + " frames");
				currFrameNum += multiplier;
				vid.set(1, currFrameNum);
			} else {
				Logger(1,"CurrFrame: " + currFrameNum);
				currFrameNum++;
			}
			progress=(int)(((double)(currFrameNum-startFrame)/(endFrame-startFrame))*100);
			Logger(1,"Time: " + TimeToString(vid.get(0)) +"	Progress: " + progress + "%");
			//TimeToString((double)(currFrameNum-startFrame)*1000/30)
			//mainFrame.setSliderProgress(progress);

			for (int i = 0; i < contours.size() && motion == false; i++) {
				// check size of areas inside of contours (marking movement) in
				// the delta frame
				// TODO add motion sensitivity
				int contourSize = (int) Imgproc.contourArea(contours.get(i));
				if (contourSize < 5000 - sensitivity || contourSize > 5000 + sensitivity) {
					motion = false;
				} 	else if (currFrameNum - lastCodedFrameNum > fps * minTimeBetweenKeyframes) {
					motion = true;
					Logger(2, "Size of contour found: " + contourSize);
					movementGuess = "Horse" + "," +  contours.size() + "," + contourSize;
					Rect boundingRectangle = Imgproc.boundingRect(contours.get(i));
					Imgproc.rectangle(frame, boundingRectangle.tl(), boundingRectangle.br(), new Scalar(0, 255, 0, 255),
							3);

				}
			}

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (motion) {
				timers=System.currentTimeMillis();
				//displayImage(Mat2BufferedImage(frame));
				timere=System.currentTimeMillis();
				timetodisplayframes+=timere-timers;
				// Found Motion
				lastCodedFrameNum = currFrameNum;
				roiMat = roiExtractor(frame);
				
				timers=System.currentTimeMillis();
				//displayImage(Mat2BufferedImage(roiMat));
				timere=System.currentTimeMillis();

				lastCodedFrame = gray.clone();
				time = TimeToString(vid.get(0));

				Logger(1, "Movement at " + time + " Guess: " + movementGuess);

				pw.write(time + "," + movementGuess + "\n");
				motion = false;
				;
			}
			timers=System.currentTimeMillis();
		}
		
		Logger(2, "Skipped Frames: " + skippedFrames);// TODO remove
																// debug line
		Logger(1, "Time to open " + TimeToString(timetoopenframes) + " , Time to process " + TimeToString(timetoprocessframes) + " , "
				+ "Time to display " + TimeToString(timetodisplayframes));
		long eTime=System.currentTimeMillis();
		
		String summary[]={("Finished analyzing in " + TimeToString(eTime-sTime) + ".\n" + "Saved to file: " + outFileName),outFileName};
		Logger(2,summary[0]);
		pw.close();
		return summary;
	}

	protected void Logger(int event, String text){
		switch(event){
		case 1: 
			System.out.print("[DEBUG] ");
			break;
		case 2: 
			System.out.print("[EVENT] ");
			break;
		default:
			break;
		}
		System.out.println(text);
	}
	
	public void WriteToFile(String text){
		PrintWriter pw = null;


		
		pw.write(text);

	}
	
	
	public String TimeToString(double timeD){
		StringBuilder sb=new StringBuilder();
		int time_msec = (int) (timeD % 1000);
		int time_sec = (int) (timeD / 1000 % 60);
		int time_min = (int) (timeD / 1000 / 60 % 60);
		int time_hour = (int) (timeD / 1000 / 3600);
		if (time_hour > 9)
			sb.append(String.valueOf(time_hour));
		else
			sb.append('0' + String.valueOf(time_hour));
		sb.append(':');
		if (time_min > 9)
			sb.append(String.valueOf(time_min));
		else
			sb.append('0' + String.valueOf(time_min));
		sb.append(':');
		if (time_sec > 9)
			sb.append(String.valueOf(time_sec));
		else
			sb.append('0' + String.valueOf(time_sec));
		//sb.append(':');
		//sb.append(String.valueOf(time_msec));
		return sb.toString();
	}
	
	public Mat roiExtractor(Mat originalMat) {
		/*
		 * try { originalMat = BufferedImage2Mat(originalImg); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
		// displayImage(Mat2BufferedImage(originalMat));
		Point topLeft = new Point(originalMat.width() / 2 - 120, 0);
		Point bottomRight = new Point(originalMat.width() / 2 + 100, 40);
		Rect roiRect = new Rect(topLeft, bottomRight);
		Mat cropped = null;
		try {
			cropped = new Mat(originalMat, roiRect);
		} catch (CvException e) {
			System.out.println(e.toString());
		}
		return cropped;
	}

	public static Mat BufferedImage2Mat(BufferedImage image) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", byteArrayOutputStream);
		byteArrayOutputStream.flush();
		return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()),
				Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
	}

	public BufferedImage Mat2BufferedImage(Mat m) {
		// Fastest code
		// output can be assigned either to a BufferedImage or to an Image

		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (m.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = m.channels() * m.cols() * m.rows();
		byte[] b = new byte[bufferSize];
		m.get(0, 0, b); // get all the pixels
		BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		return image;
	}

	public void displayImage(Image img2) {

		// BufferedImage img=ImageIO.read(new File("/HelloOpenCV/lena.png"));
		vidFrame=new JFrame();
		vidFrame.setSize(img2.getWidth(null), img2.getHeight(null));
		ImageIcon icon = new ImageIcon(img2);

		JLabel lbl = new JLabel();

		lbl.setIcon(icon);
		vidFrame.add(lbl);
		vidFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		vidFrame.setVisible(true);
	}
}
