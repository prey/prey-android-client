package com.prey.actions.screenshot;


import android.content.Context;
 
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
 
import android.view.View;
 

public class ColorPanelView extends View{

	  private static float a = 1.0F;
	  private int b = -9539986;
	  private int c = -16777216;
	  private Paint d = new Paint();
	  private Paint e = new Paint();
	  private RectF f;
	  private RectF g;
	  
	  public ColorPanelView(Context paramContext)
	  {
	    this(paramContext, null);
	  }

	  public ColorPanelView(Context paramContext, AttributeSet paramAttributeSet)
	  {
	    this(paramContext, paramAttributeSet, 0);
	  }

	  public ColorPanelView(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
	  {
	    super(paramContext, paramAttributeSet, paramInt);
	    a = getContext().getResources().getDisplayMetrics().density;
	  }

	  public final void a(int paramInt)
	  {
	    this.c = paramInt;
	    invalidate();
	  }

	  public final void b(int paramInt)
	  {
	    this.b = paramInt;
	    invalidate();
	  }

	  protected void onDraw(Canvas paramCanvas)
	  {
	    RectF localRectF = this.g;
	    this.d.setColor(this.b);
	    paramCanvas.drawRect(this.f, this.d);
	 
	    this.e.setColor(this.c);
	    paramCanvas.drawRect(localRectF, this.e);
	  }

	  protected void onMeasure(int paramInt1, int paramInt2)
	  {
	    setMeasuredDimension(View.MeasureSpec.getSize(paramInt1), View.MeasureSpec.getSize(paramInt2));
	  }

	  protected void onSizeChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
	  {
	    super.onSizeChanged(paramInt1, paramInt2, paramInt3, paramInt4);
	    this.f = new RectF();
	    this.f.left = getPaddingLeft();
	    this.f.right = (paramInt1 - getPaddingRight());
	    this.f.top = getPaddingTop();
	    this.f.bottom = (paramInt2 - getPaddingBottom());
	    RectF localRectF = this.f;
	    float f1 = 1.0F + localRectF.left;
	    float f2 = 1.0F + localRectF.top;
	    float f3 = localRectF.bottom - 1.0F;
	    this.g = new RectF(f1, f2, localRectF.right - 1.0F, f3);
	    
	  }

}
