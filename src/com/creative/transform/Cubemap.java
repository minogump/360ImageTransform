package com.creative.transform;


import com.sun.javafx.geom.Vec2d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

/**
 * Created by mino on 16-6-6.
 */
public class Cubemap {
    private String filename;
    private int width;
    private int height;
    private VideoCapture cap;

    /**
     *
     * @param filename the file's path
     */
    Cubemap(String filename) {
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

    public void toFisheye(String savepath) {
        VideoWriter oVideoWriter = new VideoWriter();
        oVideoWriter.open(savepath, VideoWriter.fourcc('D', 'I', 'V', 'X'), 24, new Size(this.width * 2 / 3, this.height / 2));
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
                Mat outputMat = Mat.zeros(this.height / 2, this.width * 2 / 3, frame.type());
                cubeToFish(frame, outputMat);
                System.out.print(count + "\r");
                oVideoWriter.write(outputMat);
            } else {
                break;
            }
        }
        oVideoWriter.release();
    }

    /**
     * Convert the cubemap image to fisheye image.
     * @param inMat the input frame's matrix
     * @param outMat the output frame's matrix
     */
    private void cubeToFish(Mat inMat, Mat outMat) {
        int outSizeY = outMat.rows(), outSizeX = outMat.cols();
        int radius = width / 6;     // 256
        int edge = width / 3;       //  512
        for (int i = 0; i < outSizeX; i++) {
            for (int j = 0; j < outSizeY; j++) {
                double originX, originY, originZ;
                if (i < radius * 2 && j < height / 2) {           // back face
                    if (Math.hypot(radius - i, radius - j) <= radius) {
                        double fishY = radius - j, fishZ = radius - i;
                        double r = Math.hypot(fishY, fishZ);
                        double theta = Math.asin(r / radius);
                        double ballX = -radius * Math.cos(theta), ballY = fishY, ballZ = fishZ;
                        if (Math.abs(ballX) >= Math.abs(ballY) && Math.abs(ballX) >= Math.abs(ballZ)) {
                            originX = -radius;
                            originZ = ballZ * (originX / ballX);
                            originY = ballY * (originX / ballX);
                            Vec2d v2d = getXY((int) originX, (int) originY, (int) originZ);
                            outMat.put(j, i, inMat.get((int) (v2d.y), (int) (v2d.x)));
                        } else if (Math.abs(ballY) >= Math.abs(ballX) && Math.abs(ballY) >= Math.abs(ballZ)) {
                            originY = (ballY / Math.abs(ballY)) * radius;
                            originZ = ballZ * (originY / ballY);
                            originX = ballX * (originY / ballY);
                            Vec2d v2d = getXY((int) originX, (int) originY, (int) originZ);
                            outMat.put(j, i, inMat.get((int) (v2d.y), (int) (v2d.x)));
                        } else if (Math.abs(ballZ) >= Math.abs(ballX) && Math.abs(ballZ) >= Math.abs(ballY)) {
                            originZ = (ballZ / Math.abs(ballZ)) * radius;
                            originY = ballY * (originZ / ballZ);
                            originX = ballX * (originZ / ballZ);
                            Vec2d v2d = getXY((int) originX, (int) originY, (int) originZ);
                            outMat.put(j, i, inMat.get((int) (v2d.y), (int) (v2d.x)));
                        }
                    }
                } else if (i >= radius * 2 && i < radius * 4 && j < height / 2) {
                    double fishY = radius - j, fishZ = (i - radius * 2) - radius;
                    double r = Math.hypot(fishY, fishZ);
                    double theta = Math.asin(r / radius);
                    double ballX = radius * Math.cos(theta), ballY = fishY, ballZ = fishZ;
                    if (Math.abs(ballX) >= Math.abs(ballY) && Math.abs(ballX) >= Math.abs(ballZ)) {
                        originX = radius;
                        originZ = ballZ * (originX / ballX);
                        originY = ballY * (originX / ballX);
                        Vec2d v2d = getXY((int) originX, (int) originY, (int) originZ);
                        outMat.put(j, i, inMat.get((int) (v2d.y), (int) (v2d.x)));
                    } else if (Math.abs(ballY) >= Math.abs(ballX) && Math.abs(ballY) >= Math.abs(ballZ)) {
                        originY = (ballY / Math.abs(ballY)) * radius;
                        originZ = ballZ * (originY / ballY);
                        originX = ballX * (originY / ballY);
                        Vec2d v2d = getXY((int) originX, (int) originY, (int) originZ);
                        outMat.put(j, i, inMat.get((int) (v2d.y), (int) (v2d.x)));
                    } else if (Math.abs(ballZ) >= Math.abs(ballX) && Math.abs(ballZ) >= Math.abs(ballY)) {
                        originZ = (ballZ / Math.abs(ballZ)) * radius;
                        originY = ballY * (originZ / ballZ);
                        originX = ballX * (originZ / ballZ);
                        Vec2d v2d = getXY((int) originX, (int) originY, (int) originZ);
                        outMat.put(j, i, inMat.get((int) (v2d.y), (int) (v2d.x)));
                    }
                }
            }
        }
    }

    /**
     * For each pair of X, Y and Z, return the X, Y coordinate of the input image.
     * @param X X coordinate in 3D world
     * @param Y Y coordinate in 3D world
     * @param Z Z coordinate in 3D world
     * @return the X, Y pair in input image
     */
    private Vec2d getXY(int X, int Y, int Z) {
        int edge = width / 3;
        int halfEdge = edge / 2;
        Vec2d v2d = new Vec2d();
        if (X == halfEdge) {
            v2d.x = edge + halfEdge - Y;
            v2d.y = edge + halfEdge - Z;
        } else if (X == -halfEdge) {
            v2d.x = 2 * edge + halfEdge + Y;
            v2d.y = edge + halfEdge - Z;
        } else if (Y == halfEdge) {
            v2d.x = edge + halfEdge + X;
            v2d.y = halfEdge - Z;
        } else if (Y == -halfEdge) {
            v2d.x = halfEdge - X;
            v2d.y = halfEdge - Z;
        } else if (Z == halfEdge) {
            v2d.x = 2 * edge + halfEdge + Y;
            v2d.y = halfEdge + X;
        } else if (Z == -halfEdge) {
            v2d.x = halfEdge - Y;
            v2d.y = edge + halfEdge - X;
        }
        if (v2d.x >= width) v2d.x = width - 1;
        if (v2d.x < 0) v2d.x = 0;
        if (v2d.y < 0) v2d.y = 0;
        if (v2d.y >= height) v2d.y = height - 1;
        return v2d;
    }

}
