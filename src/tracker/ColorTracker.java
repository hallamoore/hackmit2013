package tracker;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class ColorTracker {
	// HSV threshold values for color detection
	static Scalar RED_MIN = new Scalar(170, 100, 100);
	static Scalar RED_MAX = new Scalar(180, 200, 200);
	static Scalar BLUE_MIN = new Scalar(115, 100, 100);
	static Scalar BLUE_MAX = new Scalar(125, 200, 200);

	// store the camera
	VideoCapture camera;

	// state variables
	public Boolean cursor = false; // true if red is in the image
	public Boolean click = false; // true if blue is in the image
	public double xPercent = 0; // the x-coordinate as a percentage of the total
								// width
	public double yPercent = 0; // the y-coordinate as a percentage of the total
								// height
	
	private Boolean stopped = false;

	public void run() throws InterruptedException {
		System.loadLibrary("opencv_java246"); // something about native java

		// find the camera, wait for it to initialize, and then access it
		camera = new VideoCapture(0);
		Thread.sleep(1000);
		camera.open(0);
		if (!camera.isOpened()) {
			throw new RuntimeException("Could not open the web cam.");
		}
		while (camera.isOpened()) {
			// Get a frame from the camera
			Mat frame = new Mat();
			camera.read(frame);
			// Highgui.imwrite("camera_pic.jpg", frame); //DEBUG

			// Increase the contrast gain by 1.5
			Mat contrast = new Mat();
			frame.convertTo(contrast, -1, 1.5);
			// Highgui.imwrite("camera__pic_contrast.jpg", contrast); //DEBUG

			// convert the image to HSV for easier color detection
			Mat hsv = new Mat();
			Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);

			/***** CURSOR ******/
			// Threshold the image based on the HSV values of the desired color
			// (red for cursor)
			Mat hsvThresh_cursor = new Mat();
			Core.inRange(hsv, RED_MIN, RED_MAX, hsvThresh_cursor);
			Highgui.imwrite("camera_pic_red.jpg", hsvThresh_cursor);// DEBUG

			// Expand the white areas of the threshold (the areas that were
			// the desired color) to make finding the contours easier
			Imgproc.dilate(hsvThresh_cursor, hsvThresh_cursor, new Mat());

			// Find the contours!
			List<MatOfPoint> contours_cursor = new ArrayList<MatOfPoint>();
			Mat convert_cursor = new Mat();
			hsvThresh_cursor.convertTo(convert_cursor, CvType.CV_32SC1); // need
																			// image
																			// to
																			// be
																			// 32SC1,
																			// not
																			// sure
																			// why.
			Imgproc.findContours(convert_cursor, contours_cursor, new Mat(),
					Imgproc.RETR_FLOODFILL, Imgproc.CHAIN_APPROX_SIMPLE);

			// Draw all the contours such that they are filled in.
			Mat contourImg_cursor = new Mat(convert_cursor.size(),
					convert_cursor.type());
			for (int i = 0; i < contours_cursor.size(); i++) {
				Imgproc.drawContours(contourImg_cursor, contours_cursor, i,
						new Scalar(255, 255, 255), -1);
			}
			Highgui.imwrite("camera_pic_red_contours.jpg", contourImg_cursor); // DEBUG

			// Find the contours of the filled in mass of previous contours
			Imgproc.findContours(contourImg_cursor, contours_cursor, new Mat(),
					Imgproc.RETR_FLOODFILL, Imgproc.CHAIN_APPROX_SIMPLE);
			if (contours_cursor.size() > 0) {
				// Find the largest of those contours
				double max = 0;
				int maxCont = 0;
				for (int i = 0; i < contours_cursor.size(); i++) {
					double area = Imgproc.contourArea(contours_cursor.get(i));
					if (area > max) {
						max = area;
						maxCont = i;
					}
				}
				// If it's less than 20 pixels in area, don't acknowledge that
				// it's there
				if (max > 20) {
					this.cursor = true;
					// Get the rectangle that fits around the boundary of the
					// largest contour
					// Use it to figure out the center (x, y) of the contour
					Rect bound = Imgproc.boundingRect(contours_cursor
							.get(maxCont));
					double y = bound.y + (bound.height / 2);
					double x = bound.x + (bound.width / 2);
					this.xPercent = x / frame.cols();
					this.yPercent = y / frame.rows();
					Core.circle(frame, new Point(x, y), 50, new Scalar(255, 0,
							0)); // DEBUG
					Highgui.imwrite("camera_pic_final.jpg", frame); // DEBUG
					Thread.sleep(500);// DEBUG
				} else {
					this.cursor = false;
				}
			} else { // if there aren't any contours, the desired color isn't in
						// the image
				this.cursor = false;
			}

			/***** CLICK ******/
			// Threshold the image based on the HSV values of the desired color
			// (blue for click)
			Mat hsvThresh_click = new Mat();
			Core.inRange(hsv, BLUE_MIN, BLUE_MAX, hsvThresh_click);
			Highgui.imwrite("camera_pic_red.jpg", hsvThresh_click);// DEBUG

			// Expand the white areas of the threshold (the areas that were
			// the desired color) to make finding the contours easier
			Imgproc.dilate(hsvThresh_click, hsvThresh_click, new Mat());

			// Find the contours!
			List<MatOfPoint> contours_click = new ArrayList<MatOfPoint>();
			Mat convert_click = new Mat();
			hsvThresh_click.convertTo(convert_click, CvType.CV_32SC1); // need
																		// image
																		// to be
																		// 32SC1,
																		// not
																		// sure
																		// why.
			Imgproc.findContours(convert_click, contours_click, new Mat(),
					Imgproc.RETR_FLOODFILL, Imgproc.CHAIN_APPROX_SIMPLE);

			// Draw all the contours such that they are filled in.
			Mat contourImg_click = new Mat(convert_click.size(),
					convert_click.type());
			for (int i = 0; i < contours_click.size(); i++) {
				Imgproc.drawContours(contourImg_click, contours_click, i,
						new Scalar(255, 255, 255), -1);
			}
			Highgui.imwrite("camera_pic_red_contours.jpg", contourImg_click); // DEBUG

			// Find the contours of the filled in mass of previous contours
			Imgproc.findContours(contourImg_click, contours_click, new Mat(),
					Imgproc.RETR_FLOODFILL, Imgproc.CHAIN_APPROX_SIMPLE);
			if (contours_click.size() > 0) {
				// Find the largest of those contours
				double max = 0;
				for (int i = 0; i < contours_click.size(); i++) {
					double area = Imgproc.contourArea(contours_click.get(i));
					if (area > max) {
						max = area;
					}
				}
				// If it's less than 20 pixels in area, don't acknowledge that
				// it's there
				if (max > 20) {
					this.click = true;
				} else {
					this.click = false;
				}

			} else { // if there aren't any contours, the desired color isn't in
						// the image
				this.click = false;
			}
			if(this.stopped){
				camera.release();
			}
		}

		// Release the camera so the program properly terminates
		camera.release();
	}

	public String update() {
		String message = "";
		if (this.cursor) {
			message += " POST cursor," + xPercent + ',' + yPercent;
		}
		if (this.click) {
			message += " POST click";
		}
		return message;
	}

	public void stop() {
		this.stopped = true;
	}
}