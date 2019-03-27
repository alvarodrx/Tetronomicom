package com.alvarodrx.tetris;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class Tetris extends AppCompatActivity {


    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 12;
    private final int INITIAL_DELAY = 100;
    private final int PERIOD_INTERVAL = 300;

    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private int numLinesRemoved = 0;
    private int curX = 0;
    private int curY = 0;
    private Shape curPiece;
    private Shape.Tetrominoe[] board;
    private GridLayout gl;

    Handler handler;

    public void rotateLeft(View v){
        tryMove(curPiece, curX - 1, curY);
    }

    public void rotateRight(View v){
        tryMove(curPiece.rotateRight(), curX, curY);
    }

    public void moveRight(View v){
        tryMove(curPiece, curX + 1, curY);
    }

    public void moveLeft(View v){
        tryMove(curPiece, curX - 1, curY);
    }

    public void moveDown(View v){
        dropDown();
    }

    public void moveOLDown(View v){
        oneLineDown();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gl = (GridLayout) findViewById(R.id.gridl1);
        handler = new Handler();
        initBoard();
    }

    private void initBoard() {

        //setFocusable(true);
        //timer = new Timer();
        //timer.scheduleAtFixedRate(new Tetris.ScheduleTask(), INITIAL_DELAY, PERIOD_INTERVAL);


        Runnable run = new Runnable() {
            @Override
            public void run() {
                if(!isPaused){
                    doGameCycle();
                    handler.postDelayed(this,1000);
                }


            }
        };
        handler.postDelayed(run,1000);

        curPiece = new Shape();

        //statusbar = parent.getStatusBar();
        board = new Shape.Tetrominoe[BOARD_WIDTH * BOARD_HEIGHT];
        //addKeyListener(new TAdapter());
        clearBoard();
    }

    private int squareWidth() {
        return 100;
    }

    private int squareHeight() {
        return 100;
    }

    private Shape.Tetrominoe shapeAt(int x, int y) {
        return board[(y * BOARD_WIDTH) + x];
    }

    public void start() {

        isStarted = true;
        clearBoard();
        newPiece();
    }

    private void pause() {

        if (!isStarted) {
            return;
        }

        isPaused = !isPaused;

        if (isPaused) {

            //statusbar.setText("paused");
        } else {

            //statusbar.setText(String.valueOf(numLinesRemoved));
        }
    }

    private void doDrawing() {
        int boardTop = 1200;

        for (int i = 0; i < BOARD_HEIGHT; ++i) {

            for (int j = 0; j < BOARD_WIDTH; ++j) {

                Shape.Tetrominoe shape = shapeAt(j, BOARD_HEIGHT - i - 1);

                if (shape != Shape.Tetrominoe.NoShape) {
                    Log.d("MSG", "Llenos");
                    drawSquare(gl, j, i , shape);
                }else{
                    Log.d("MSG", "Vacios");
                    drawVacSquare(gl, j,  i);
                }
            }
        }


        if (curPiece.getShape() != Shape.Tetrominoe.NoShape) {

            for (int i = 0; i < 4; ++i) {

                int x = curX + curPiece.x(i);
                int y = curY - curPiece.y(i);
                if(y != BOARD_HEIGHT)
                    drawSquare(gl, x, (BOARD_HEIGHT - y -1),
                        curPiece.getShape());
            }
        }
    }
    private void drawSquare(GridLayout g, int x, int y,
                            Shape.Tetrominoe shape) {
        Log.d("drow",": "+x+" "+y);
        int colors[] = {
                R.drawable.block_vacio,
                R.drawable.block_rojo,
                R.drawable.block_amarillo,
                R.drawable.block_azul,
                R.drawable.block_purpura,
                R.drawable.block_rosa,
                R.drawable.block_verde,
                R.drawable.block_vagua
        };

        int color = colors[shape.ordinal()];
        ImageView imgAt = (ImageView) gl.getChildAt(y*BOARD_WIDTH+x);
        imgAt.setImageResource(color);
    }

    private void drawVacSquare(GridLayout g, int x, int y) {
        Log.d("MSG", "draw2");
        ImageView imgAt = (ImageView) gl.getChildAt(y*BOARD_WIDTH+x);
        imgAt.setImageResource(R.drawable.block_vacio);
    }


    private void dropDown() {

        int newY = curY;

        while (newY > 0) {

            if (!tryMove(curPiece, curX, newY - 1)) {

                break;
            }

            --newY;
        }

        pieceDropped();
    }

    private void oneLineDown() {
        Log.d("MSG", "OnLineDown");
        if (!tryMove(curPiece, curX, curY - 1)) {

            pieceDropped();
        }
    }

    private void pieceDropped() {
        Log.d("MSG2", "piece dropped");
        for (int i = 0; i < 4; ++i) {

            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            board[(y * BOARD_WIDTH) + x] = curPiece.getShape();
        }

        removeFullLines();

        if (!isFallingFinished) {
            newPiece();
        }
    }

    private void gameOver(){
        isPaused = true;
        isStarted = false;
        Toast toast1 = Toast.makeText(getApplicationContext(),"Game Over", Toast.LENGTH_SHORT);
        toast1.show();
    }
    private void newPiece() {

        curPiece.setRandomShape();
        curX = BOARD_WIDTH / 2;
        curY = BOARD_HEIGHT - 1 + curPiece.minY();
        Log.d("MSG","newPiece");
        if (!tryMove(curPiece, curX, curY)) {
            Log.d("MSG","newPiece - - -");
            curPiece.setShape(Shape.Tetrominoe.NoShape);
            gameOver();
        }
    }

    private boolean tryMove(Shape newPiece, int newX, int newY) {
        Log.d("MSG","tryMove1");
        for (int i = 0; i < 4; ++i) {

            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);

            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
                return false;
            }

            if (shapeAt(x, y) != Shape.Tetrominoe.NoShape) {
                return false;
            }
        }
        Log.d("MSG","tryMove2");
        curPiece = newPiece;
        curX = newX;
        curY = newY;

        repaint();

        return true;
    }

    private void removeFullLines() {
        Log.d("MSG", "removeFullLines");
        int numFullLines = 0;

        for (int i = BOARD_HEIGHT - 1; i >= 0; --i) {
            boolean lineIsFull = true;

            for (int j = 0; j < BOARD_WIDTH; ++j) {

                if (shapeAt(j, i) == Shape.Tetrominoe.NoShape) {

                    lineIsFull = false;
                    break;
                }
            }

            if (lineIsFull) {

                ++numFullLines;

                for (int k = i; k < BOARD_HEIGHT - 1; ++k) {
                    for (int j = 0; j < BOARD_WIDTH; ++j) {

                        board[(k * BOARD_WIDTH) + j] = shapeAt(j, k + 1);
                    }
                }
            }
        }

        if (numFullLines > 0) {

            numLinesRemoved += numFullLines;
            //statusbar.setText(String.valueOf(numLinesRemoved));
            isFallingFinished = true;
            curPiece.setShape(Shape.Tetrominoe.NoShape);
            repaint();
        }
    }



    private void doGameCycle() {
        Log.d("MSG","ciclo");
        update();
        repaint();
    }

    private void update() {

        if (isPaused) {
            return;
        }

        if (isFallingFinished) {

            isFallingFinished = false;
            newPiece();
        } else {

            oneLineDown();
        }
    }

    /*private class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            System.out.println("key pressed");

            if (!isStarted || curPiece.getShape() == Tetrominoe.NoShape) {
                return;
            }

            int keycode = e.getKeyCode();

            if (keycode == KeyEvent.VK_P) {
                pause();
                return;
            }

            if (isPaused) {
                return;
            }

            switch (keycode) {

                case KeyEvent.VK_LEFT:
                    tryMove(curPiece, curX - 1, curY);
                    break;

                case KeyEvent.VK_RIGHT:
                    tryMove(curPiece, curX + 1, curY);
                    break;

                case KeyEvent.VK_DOWN:
                    tryMove(curPiece.rotateRight(), curX, curY);
                    break;

                case KeyEvent.VK_UP:
                    tryMove(curPiece.rotateLeft(), curX, curY);
                    break;

                case KeyEvent.VK_SPACE:
                    dropDown();
                    break;

                case KeyEvent.VK_D:
                    oneLineDown();
                    break;
            }
        }
    }*/

    private void repaint(){
        Log.d("MSG", "Repaint");
        doDrawing();
    }


    private void clearBoard() {
        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; ++i) {
            board[i] = Shape.Tetrominoe.NoShape;
        }
        gl.removeAllViews();
        for(int i = 0; i<120; i++){
            ImageView img = new ImageView(this);
            img.setLayoutParams(new ViewGroup.LayoutParams(100,100));
            if(i < 10 || i > 110 || i%10 == 0 || i%10 == 9){
                img.setImageResource(R.drawable.block_lateral);
            }else{
                img.setImageResource(R.drawable.block_vacio);
            }

            img.setAdjustViewBounds(true);
            gl.addView(img, i);

        }
    }

}


