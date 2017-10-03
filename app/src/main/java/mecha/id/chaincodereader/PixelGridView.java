package mecha.id.chaincodereader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

public class PixelGridView extends View {
    private int numColumns, numRows;
    private int cellWidth, cellHeight;
    private Paint blackPaint = new Paint();
    private boolean[][] cellChecked;

    private static String LOG = "WOW";
    private String chainCode;
    private Map<Integer,List<Integer>> toFlood;

    public PixelGridView(Context context) {
        this(context, null);
    }

    public PixelGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
        calculateDimensions();
    }

    public int getNumColumns() {
        return numColumns;
    }

    public void setNumRows(int numRows) {
        this.numRows = numRows;
        calculateDimensions();
    }

    public int getNumRows() {
        return numRows;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDimensions();
    }

    private void calculateDimensions() {
        if (numColumns < 1 || numRows < 1) {
            return;
        }

        cellWidth = getWidth() / numColumns;
        cellHeight = getHeight() / numRows;

        cellChecked = new boolean[numColumns][numRows];

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);

        if (numColumns == 0 || numRows == 0) {
            return;
        }

        int width = getWidth();
        int height = getHeight();

        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < numRows; j++) {
                if (cellChecked[i][j]) {

                    canvas.drawRect(i * cellWidth, j * cellHeight,
                            (i + 1) * cellWidth, (j + 1) * cellHeight,
                            blackPaint);
                }
            }
        }

        for (int i = 1; i < numColumns; i++) {
            canvas.drawLine(i * cellWidth, 0, i * cellWidth, height, blackPaint);
        }

        for (int i = 1; i < numRows; i++) {
            canvas.drawLine(0, i * cellHeight, width, i * cellHeight, blackPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int column = (int)(event.getX() / cellWidth);
            int row = (int)(event.getY() / cellHeight);

            cellChecked[column][row] = !cellChecked[column][row];
            invalidate();
        }

        return true;
    }

    public void clearGrid(){
        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < numRows; j++) {
                cellChecked[i][j] = false;
            }
        }

        invalidate();
        Log.i(LOG,"clearGrid");
    }

    public String getChainCode() {

        //Log.i(LOG,"init getChainCode()");

        String chainCode = "";
        int pixelFound = 0;
        int startI = 0,startJ = 0,latestChainCodeNumber;

        //Find starting point for the chain code
        outerloop:
        for (int j = 0; j < numRows; j++) {
            for (int i = 0; i < numColumns; i++) {
                if(cellChecked[i][j]){
                    startI = i;
                    startJ = j;
                    pixelFound += 1;
                    break outerloop;
                }
            }
        }

        if(pixelFound>0){
            Log.i(LOG,"Chain code starting point : "+String.valueOf(startI)+","+String.valueOf(startJ));
            chainCode = getChainCodeString(startI,startJ,startI,startJ,4);
        }else{
            chainCode = "Pixel not found";
        }



        return chainCode;
    }

    private String getChainCodeString(int startI,int startJ,int currentIParam, int currentJParam, int initNextDirection) {

        //Log.i(LOG,"init getChainCodeString()");

        String chainCode = "";
        int nextDirection,initNextDirectionAfter,currentI,currentJ,iToCheck,jToCheck;

        currentI = currentIParam;
        currentJ = currentJParam;

        for(int i=initNextDirection;i<initNextDirection+8;i++){
            if(i>9){
                nextDirection = i - 8;
            }else{
                nextDirection = i;
            }

            switch(nextDirection){
                case 2:
                    iToCheck = currentI;
                    jToCheck = currentJ-1;
                    initNextDirectionAfter = 9;
                    break;
                case 3:
                    iToCheck = currentI+1;
                    jToCheck = currentJ-1;
                    initNextDirectionAfter = 9;
                    break;
                case 4:
                    iToCheck = currentI+1;
                    jToCheck = currentJ;
                    initNextDirectionAfter = 3;
                    break;
                case 5:
                    iToCheck = currentI+1;
                    jToCheck = currentJ+1;
                    initNextDirectionAfter = 3;
                    break;
                case 6:
                    iToCheck = currentI;
                    jToCheck = currentJ+1;
                    initNextDirectionAfter = 5;
                    break;
                case 7:
                    iToCheck = currentI-1;
                    jToCheck = currentJ+1;
                    initNextDirectionAfter = 5;
                    break;
                case 8:
                    iToCheck = currentI-1;
                    jToCheck = currentJ;
                    initNextDirectionAfter = 7;
                    break;
                case 9:
                    iToCheck = currentI-1;
                    jToCheck = currentJ-1;
                    initNextDirectionAfter = 7;
                    break;
                default:
                    iToCheck = currentI;
                    jToCheck = currentJ;
                    initNextDirectionAfter = 3;
            }

            if(cellChecked[iToCheck][jToCheck]){
                chainCode += String.valueOf(nextDirection);
                //Log.i(LOG,"haha : "+String.valueOf(iToCheck)+","+String.valueOf(jToCheck)+","+String.valueOf(startI)+","+String.valueOf(startJ));
                if(iToCheck!=startI||jToCheck!=startJ){
                    //Log.i(LOG,"beda ama titik awal");
                    chainCode += getChainCodeString(startI,startJ,iToCheck,jToCheck,initNextDirectionAfter);
                }
                break;
            }
        }

        return chainCode;
    }

    public String getPredictedCharacter(String chainCode) {

        Log.i(LOG,"init getPredictedCharacter() "+chainCode);

        String predictedCharacter = "-";

        String pattern;
        Matcher m;


        try {

            if (Pattern.compile("4+6+5+4*6+8+2+").matcher(chainCode).matches()) {
                predictedCharacter = "L";
            }

            if (Pattern.compile("4+6+8+2+4*3+2+").matcher(chainCode).matches()) {
                predictedCharacter = "J";
            }

            m = Pattern.compile("(4+)(6+)8+2+").matcher(chainCode);
            if (m.matches()) {
                if( ((double)m.group(2).length()/(double)m.group(1).length()) > 1.5 ){
                    predictedCharacter = "I / 1";
                }else{
                    predictedCharacter = "0 / O";
                }
            }

            if (Pattern.compile("4+6+8*7+6+8+2+9+8*2+").matcher(chainCode).matches()) {
                predictedCharacter = "T";
            }

            m = Pattern.compile("4+(6+)8*7+6*5+4*(6+)8+2+").matcher(chainCode);
            if (m.matches()) {
                if( ((double)m.group(2).length()/(double)m.group(1).length()) >= 2 ){
                    predictedCharacter = "6";
                }else{
                    predictedCharacter = "C";
                }
            }

            if (Pattern.compile("4+6+5+4*3+2+4+6+8+2+").matcher(chainCode).matches()) {
                predictedCharacter = "U";
            }

            m = Pattern.compile("4+(6+)8*7+(6*)5+4*(6+)8+(2+)4*3+(2*)9+8*(2+)").matcher(chainCode);
            if (m.matches()) {
                if( (double)m.group(6).length() > (((double)m.group(1).length()+(double)m.group(2).length())) ){
                    predictedCharacter = "5 / S";
                }

                if( (double)m.group(6).length() < (((double)m.group(1).length()+(double)m.group(2).length())) ){
                    predictedCharacter = "2";
                }

                if( ((double)m.group(1).length() == (double)m.group(6).length()) && ((double)m.group(3).length() == (double)m.group(4).length()) ){
                    predictedCharacter = "8";
                }
            }

            if (Pattern.compile("4+6+8+2+4*3+2*9+8*2+4*3+2*9+8*2+").matcher(chainCode).matches()) {
                predictedCharacter = "3";
            }

            if (Pattern.compile("4+6*5+4*3+2*4+6+8+2*9+8+2+").matcher(chainCode).matches()) {
                predictedCharacter = "4";
            }

            m = Pattern.compile("4+6+8+2*9+8*2+").matcher(chainCode);
            if (m.matches()) {
                predictedCharacter = "7";
            }


            if (Pattern.compile("4+6+8+2+4*3+2*9+8+2+").matcher(chainCode).matches()) {
                predictedCharacter = "9";
            }

        }catch(Exception e){
            Log.e(LOG,e.getMessage());
        }



        return predictedCharacter;
    }


}
