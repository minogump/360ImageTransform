package com.creative.transform;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;


/**
 * Created by mino on 16-6-6.
 */
public class Fisheye {
    private String filename;
    private int width;
    private int height;
    private VideoCapture cap;
    private double FOV = Math.PI * (202.7 / 180.0);

    /**
     *
     * @param filename the file's path
     */
    Fisheye(String filename) {
        this.filename = filename;
        cap = new VideoCapture(filename);
        width = (int)(cap.get(Videoio.CV_CAP_PROP_FRAME_WIDTH));
        height = (int)(cap.get(Videoio.CV_CAP_PROP_FRAME_HEIGHT));
    }

    /**
     *
     * @return the file's path
     */
    public String getFilename() {
        return filename;
    }

    /**
     *
     * @return the frame's height of the video
     */
    public int getHeight() {
        return height;
    }

    /**
     *
     * @return the frame's width of the video
     */
    public int getWidth() {
        return width;
    }

    /**
     *
     * @param filename set the file's path
     */
    public void setFilename(String filename) {
        this.filename = filename;
        cap = new VideoCapture(filename);
        width = (int)(cap.get(Videoio.CV_CAP_PROP_FRAME_WIDTH));
        height = (int)(cap.get(Videoio.CV_CAP_PROP_FRAME_HEIGHT));
    }

    public void toEquirectangular(String savepath) {
        VideoWriter oVideoWriter = new VideoWriter();
        oVideoWriter.open(savepath, VideoWriter.fourcc('D', 'I', 'V', 'X'), 20, new Size(width, height));
        int count = 0;
        Mat frame = new Mat();
        if (cap.isOpened()) {
            System.out.println("Input capture is opened");
        }
        if (oVideoWriter.isOpened()) {
            System.out.println("Output writer is opened");
        }
        while (true) {
            if (cap.read(frame)) {
                count++;
                Mat out = new Mat(height, width, frame.type());
                fishToEq(frame, out);
                System.out.print(count + "\r");
                oVideoWriter.write(out);
            } else {
                break;
            }
        }
        oVideoWriter.release();
    }

    private void fishToEq(Mat inMat, Mat outMat) {
        int length = width / 2;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < 2 * length; j++) {
                double theta = 2.0 * Math.PI * ((double)j / (2.0 * length) - 0.5);
                double phi = Math.PI * ((double)i / length - 0.5);
                double sphx = Math.cos(phi) * Math.sin(theta);
                double sphy = Math.cos(phi) * Math.cos(theta);
                double sphz = Math.sin(phi);
                theta = Math.atan2(sphz, sphx);
                phi = Math.atan2(Math.hypot(sphx, sphz) , sphy);
                double r = length * phi / FOV;
                int x = (int)(0.5 * length + r * Math.cos(theta));
                int y = (int)(0.5 * length + r * Math.sin(theta));
                if (x >= 0 && x < length && y >= 0 && y < length) {
                    if (x >= 0 && x < 0.5 * length && j >= 0.5 * length && j < length) {
                        outMat.put(i, j+length, inMat.get(length - 1 - x, y));
                    } else if (x >= length / 2 && x < length && j >= length && j < 1.5 * length) {
                        outMat.put(i, j - length, inMat.get(length - 1 - x, y));
                    }
                    if (j >= 0.5 * length && j < 1.5 * length) {
                        outMat.put(i, j, inMat.get(x, width - 1 - y));
                    }
                }
            }
        }
    }

}
