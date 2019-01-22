package com.hegdeuday.sharifycore;

import java.io.File;
import java.net.URL;

/**
 *
 * @author hegdeuday
 */
public interface Controller {
    public void send(File[] files,File root);
    public void send(URL url);
    public void send(String msg);
}
