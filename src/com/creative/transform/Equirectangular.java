package com.creative.transform;

import com.sun.javafx.geom.Vec2d;
import com.sun.javafx.geom.Vec3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;
import org.opencv.core.CvType;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;


/**
 * Created by mino on 16-6-6.
 */
public class Equirectangular {
    private String filename;
    private int width;
    private int height;
    private VideoCapture cap;

    /**
     *
     * @param filename the file's path
     */
    Equirectangular(String filename) {
        this.filename = filename;
        this.cap = new VideoCapture(filename);
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



    /**
     *
     * @param savepath the path to save the video
     */
    public void toCubemap(String savepath) {
        VideoWriter oVideoWriter = new VideoWriter();
        oVideoWriter.open(savepath, VideoWriter.fourcc('D', 'I', 'V', 'X'), 20, new Size(width * 3 / 4, height));
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
                Mat out = new Mat(height, width * 3 / 4, frame.type());
                convertBack(frame, out);
                System.out.print(count + "\r");
                oVideoWriter.write(out);
            } else {
                break;
            }
        }
        oVideoWriter.release();
    }

    /**
     *
     * @param i the output pixel's width index
     * @param j the output pixel's height index
     * @param face cubemap's face, from 0 to 5
     * @param edge the face's field of view, the length of the
     * @return the corresponding xyz ordinance of the output ordinance
     */
    private Vec3d outImgToXYZ(int i, int j, int face, int edge) {
        double a = 2.0 * (double)i / (double)edge;
        double b = 2.0 * (double)j / (double)edge;
        Vec3d v = new Vec3d();
        if (face == 0) {   // right
            v.set(1.0 - a, 1.0, 1.0 - b);
        } else if (face == 1) {    // left
            v.set(a - 3.0, -1.0, 1.0 - b);
        } else if (face == 2) {    // top
            v.set(b - 1.0, a - 5.0, 1.0);
        } else if (face == 3) {    // bottom
            v.set(3.0 - b, a - 1.0, -1.0);
        } else if (face == 4) {    // front
            v.set(1.0, a - 3.0, 3.0 - b);
        } else if (face == 5) {    // back
            v.set(-1.0, 5.0 - a, 3.0 - b);
        }
        return v;
    }

    private void convertBack(Mat imgIn, Mat imgOut) {
        int inSizeY = imgIn.rows(), inSizeX = imgIn.cols();
        int outSizeY = imgOut.rows(), outSizeX = imgOut.cols();
        int edge = inSizeX / 4;   // 视角宽度
        for (int i = 0; i < outSizeX; i++) {
            int face = (i/edge);  // 0 - right, 1 - left, 2 - top
            int face2;
            for (int j = 0; j < outSizeY; j++) {
                if (j >= edge) {
                    if (face == 0) face2 = 3;    // 3 - bottom
                    else if (face == 1) face2 = 4;    // 4 - front
                    else face2 = 5;     // 5 - back
                } else {
                  face2 = face;
                }
                Vec3d v = outImgToXYZ(i, j, face2, edge);
                double theta = Math.atan2(v.y, v.x); // 水平方向夹角
                double r = Math.hypot(v.x, v.y);    // 半径
                double phi = Math.atan2(v.z, r);    // 垂直方向夹角
                // 对应原图像的坐标值
                double uf = ( 2.0 * edge * ( theta + Math.PI ) / Math.PI );
                double vf = ( 2.0 * edge * (Math.PI / 2 -  phi ) / Math.PI);
                // 非双线性插值法求像素值
                if ((int)vf < 0) vf = 0;
                else if ((int)vf >= inSizeY) vf = inSizeY - 1;
                imgOut.put(j, i, imgIn.get((int)vf, ((int)uf) % inSizeX));
            }
        }
    }

}
