package com.example.obdread;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GforceView extends SurfaceView implements SurfaceHolder.Callback {
	

	
	
	int x=0;
	int y=0;
	private GforceThread thread;
	//private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	public GforceView(Context context) {
		super(context);
		init();
		 
		
	}
	
	
	public GforceView(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    init();
	    // TODO Auto-generated constructor stub
	}

	public GforceView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    init();
	    // TODO Auto-generated constructor stub
	}
	
	 private void init(){
		    getHolder().addCallback(this);
		    thread = new GforceThread(getHolder(), this);
		  
		    setFocusable(true); // make sure we get key events
		  
		   
		  
		 
		  
		   }
		
	
	@Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
            // nothing here
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
    	 thread.setRunning(true);
         thread.start();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
    	 boolean retry = true;
         thread.setRunning(false);
         while (retry) {
                 try {
                         thread.join();
                         retry = false;
                 } catch (InterruptedException e) {
                        
                 }
         }

    }

	
	 @Override
     public void onDraw(Canvas canvas) {
		 	/*Paint p= new Paint();
              
		 	p.setColor(Color.WHITE);
              p.setAntiAlias(true);
             
              canvas.drawColor(Color.BLACK);
              
              canvas.drawCircle(200, 200, 100, p);
              //canvas.drawCircle(200, 200, 150, p);
             // canvas.drawCircle(200+(int)tutor.x, 200+(int)tutor.y, 5, p);
              //canvas.drawRect(200, 500, 400, 700, p);*/
      }
	 
	 @Override
	 public void draw(Canvas canvas){
		 Paint p= new Paint();
		 p.setColor(Color.WHITE);
         p.setAntiAlias(true);
        
         canvas.drawColor(Color.BLACK);
         
         canvas.drawCircle(150, 115, 80, p);
         p.setColor(Color.BLACK);
         canvas.drawCircle(150, 115, 75, p);
         p.setColor(Color.WHITE);
         canvas.drawCircle(150, 115, 55, p);
         p.setColor(Color.BLACK);
         canvas.drawCircle(150, 115, 50, p);
         p.setColor(Color.WHITE);
         canvas.drawCircle(150, 115, 30, p);
         p.setColor(Color.BLACK);
         canvas.drawCircle(150, 115, 25, p);
         p.setColor(Color.RED);
         canvas.drawCircle((150+y), (115+x), 13, p);
         
	 }
	 
	 public void setCoordenadaX(int valorX){
		 x=valorX;
		 
	 }
	 
	 public void setCoordenadaY(int valorY){
		 y=valorY;
		 
	 }
	 
	 public void cancelar(){
		 thread.run=false;
	 }
	 public void activar(){
		 thread.run=true;
	 }

			

}
