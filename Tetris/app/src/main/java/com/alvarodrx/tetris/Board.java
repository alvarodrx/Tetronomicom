package com.alvarodrx.tetris;

import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;
import com.alvarodrx.tetris.Shape.Tetrominoe;

public class Board extends AppCompatActivity {

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
    private Tetrominoe[] board;
    private GridLayout gl;
    private Tetris t;

    public Board(GridLayout G, Tetris T) {

        initBoard();
        gl = G;
        t = T;
    }

    private void initBoard() {

        //setFocusable(true);
        timer = new Timer();
        timer.scheduleAtFixedRate(new ScheduleTask(),
                INITIAL_DELAY, PERIOD_INTERVAL);

        curPiece = new Shape();

        //statusbar = parent.getStatusBar();
        board = new Tetrominoe[BOARD_WIDTH * BOARD_HEIGHT];
        //addKeyListener(new TAdapter());
        clearBoard();
    }

    private int squareWidth() {
        return 100;
    }

    private int squareHeight() {
        return 100;
    }

    private Tetrominoe shapeAt(int x, int y) {
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

                Tetrominoe shape = shapeAt(j, BOARD_HEIGHT - i - 1);

                if (shape != Tetrominoe.NoShape) {

                    drawSquare(gl, j, i, shape);
                }
            }
        }


        if (curPiece.getShape() != Tetrominoe.NoShape) {

            for (int i = 0; i < 4; ++i) {

                int x = curX + curPiece.x(i);
                int y = curY + curPiece.y(i);
                drawSquare(gl, 0 + x,
                        BOARD_HEIGHT - y - 1,
                        curPiece.getShape());
            }
        }
    }

    private void drawSquare(GridLayout g, int x, int y,
                            Tetrominoe shape) {


        int colors[] = {
                R.drawable.block_rojo,
                R.drawable.block_amarillo,
                R.drawable.block_azul,
                R.drawable.block_purpura,
                R.drawable.block_rosa,
                R.drawable.block_verde
        };

        int color = colors[shape.ordinal()];
        ImageView imgAt = (ImageView) gl.getChildAt(x*BOARD_WIDTH+y);
        imgAt.setImageResource(color);
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

        if (!tryMove(curPiece, curX, curY - 1)) {

            pieceDropped();
        }
    }

    private void clearBoard() {

        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; ++i) {
            board[i] = Tetrominoe.NoShape;
        }
    }

    private void pieceDropped() {

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

    private void newPiece() {

        curPiece.setRandomShape();
        curX = BOARD_WIDTH / 2 + 1;
        curY = BOARD_HEIGHT - 1 + curPiece.minY();

        if (!tryMove(curPiece, curX, curY)) {

            curPiece.setShape(Tetrominoe.NoShape);
            timer.cancel();
            isStarted = false;
            //statusbar.setText("Game over");
        }
    }

    private boolean tryMove(Shape newPiece, int newX, int newY) {

        for (int i = 0; i < 4; ++i) {

            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);

            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
                return false;
            }

            if (shapeAt(x, y) != Tetrominoe.NoShape) {
                return false;
            }
        }

        curPiece = newPiece;
        curX = newX;
        curY = newY;

        repaint();

        return true;
    }

    private void removeFullLines() {

        int numFullLines = 0;

        for (int i = BOARD_HEIGHT - 1; i >= 0; --i) {
            boolean lineIsFull = true;

            for (int j = 0; j < BOARD_WIDTH; ++j) {

                if (shapeAt(j, i) == Tetrominoe.NoShape) {

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
            curPiece.setShape(Tetrominoe.NoShape);
            repaint();
        }
    }



    private void doGameCycle() {

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

    private class ScheduleTask extends TimerTask {

        @Override
        public void run() {

            doGameCycle();
        }
    }

    private void repaint() {
        for(int i = 0; i<120; i++){
            ImageView img = new ImageView(t);
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


