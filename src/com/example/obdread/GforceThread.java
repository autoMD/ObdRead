package com.example.obdread;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GforceThread extends Thread {

    private SurfaceHolder sh;
    private GforceView view;
    public boolean run;
   
    public GforceThread(SurfaceHolder sh, GforceView view) {
            this.sh = sh;
            this.view = view;
            run = false;
    }
   
    public void setRunning(boolean run) {
            this.run = run;
    }
   
    @Override
    public void run() {
            Canvas canvas;
            while(run) {
                    canvas = null;
                    try {
                            canvas = sh.lockCanvas(null);
                            synchronized(sh) {
                                    view.draw(canvas);
                            }
                    } finally {
                            if(canvas != null)
                                    sh.unlockCanvasAndPost(canvas);         // return to a stable state
                    }
            }
    }
    
}