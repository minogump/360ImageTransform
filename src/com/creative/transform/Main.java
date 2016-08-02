package com.creative.transform;


import org.opencv.core.Core;

/**
 * Created by mino on 16-5-30.
 */
public class Main {

    private static final String equireactangular = "resources/mp4/equirectangular.mp4";
    private static final String fisheye = "resources/mp4/fisheye.mp4";
    private static final String cubemap = "resources/mp4/cubemap.mp4";

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Equirectangular eq1 = new Equirectangular(equireactangular);
        eq1.toCubemap("/home/mino/IdeaProjects/Transform/resources/output/eq2cm.avi");

        Fisheye fe = new Fisheye(fisheye);
        fe.toEquirectangular("resources/output/fe2eq.avi");

        Cubemap cb = new Cubemap(cubemap);
        cb.toFisheye("resources/output/cb2fe.avi");
    }

}
