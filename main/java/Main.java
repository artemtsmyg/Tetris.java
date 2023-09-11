import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;

/**
 * Это класс для всех фигур и их поворотов
 */
class Shape {
    /**
     *  Форма изделия
     */
    Tetrominoe pieceShape;
    /**
     * Координаты фрагмента
     */
    int coords[][];
    /**
     * Координаты фигуры во всех ее поворотах
     */
    int[][][] coordsTable;


    public Shape() {

        initShape();
    }

    void initShape() {

        coords = new int[4][2];
        setShape(Tetrominoe.NoShape);
    }


    protected void setShape(Tetrominoe shape) {

        coordsTable = new int[][][] {
                { { 0, 0 },   { 0, 0 },   { 0, 0 },   { 0, 0 } },
                { { 0, -1 },  { 0, 0 },   { -1, 0 },  { -1, 1 } },
                { { 0, -1 },  { 0, 0 },   { 1, 0 },   { 1, 1 } },
                { { 0, -1 },  { 0, 0 },   { 0, 1 },   { 0, 2 } },
                { { -1, 0 },  { 0, 0 },   { 1, 0 },   { 0, 1 } },
                { { 0, 0 },   { 1, 0 },   { 0, 1 },   { 1, 1 } },
                { { -1, -1 }, { 0, -1 },  { 0, 0 },   { 0, 1 } },
                { { 1, -1 },  { 0, -1 },  { 0, 0 },   { 0, 1 } }
        };

        for (int i = 0; i < 4 ; i++) {

            for (int j = 0; j < 2; ++j) {

                coords[i][j] = coordsTable[shape.ordinal()][i][j];
            }
        }

        pieceShape = shape;
    }

    void setX(int index, int x) { coords[index][0] = x; }
    void setY(int index, int y) { coords[index][1] = y; }
    public int x(int index) { return coords[index][0]; }
    public int y(int index) { return coords[index][1]; }
    public Tetrominoe getShape()  { return pieceShape; }


    public void setRandomShape() {

        Random r = new Random();
        int x = Math.abs(r.nextInt()) % 7 + 1;
        Tetrominoe[] values = Tetrominoe.values();
        setShape(values[x]);
    }


    public int minX() {

        int m = coords[0][0];

        for (int i=0; i < 4; i++) {

            m = Math.min(m, coords[i][0]);
        }

        return m;
    }



    public int minY() {

        int m = coords[0][1];

        for (int i=0; i < 4; i++) {

            m = Math.min(m, coords[i][1]);
        }

        return m;
    }


    public Shape rotateLeft() {

        if (pieceShape == Tetrominoe.SquareShape)
            return this;

        Shape result = new Shape();
        result.pieceShape = pieceShape;

        for (int i = 0; i < 4; ++i) {

/**
 * установите координату x новой фигуры на координату y детали
 */
            result.setX(i, y(i));
            /**
             * установите координату y новой фигуры на отрицательную координату x элемента
             */
            result.setY(i, -x(i));
        }
/**
 * верните новую форму
 */
        return result;
    }

    /**
     * поверните деталь вправо
     * @return
     */
    public Shape rotateRight() {

        if (pieceShape == Tetrominoe.SquareShape) // если фигура имеет квадратную форму
            return this; // верните форму без поворота

        Shape result = new Shape(); // создайте новую фигуру
        result.pieceShape = pieceShape; // установите форму новой фигуры в соответствии с формой детали

        for (int i = 0; i < 4; ++i) { // для количества строк в фигуре

            result.setX(i, -y(i)); // установите координату x новой фигуры на отрицательную координату y детали
            result.setY(i, x(i)); // установите координату y новой фигуры на координату x элемента
        }

        return result; // верните новую форму
    }
}

enum Tetrominoe { NoShape, ZShape, SShape, LineShape,
    TShape, SquareShape, LShape, MirroredLShape };

/**
 * Board-это класс для доски
 */
class Board extends JPanel {

    static final long serialVersionUID = 1L;
    final int BOARD_WIDTH = 10; // ширина доски
    final int BOARD_HEIGHT = 22; // высота доски
    final int INITIAL_DELAY = 100; // начальная задержка срабатывания таймера
    final int PERIOD_INTERVAL = 300; // периодический интервал таймера

    Timer timer;
    boolean isFallingFinished = false; // проверьте, закончил ли кусок падать
    boolean isStarted = false; // проверьте, началась ли игра
    boolean isPaused = false; // проверьте, приостановлена ли игра
    int numLinesRemoved = 0; // количество удаленных строк
    int curX = 0; // текущая координата x элемента
    int curY = 0; // текущая координата y элемента
    JLabel statusbar;
    Shape curPiece;
    Tetrominoe[] board;

    public Board(Tetris parent) {

        initBoard(parent);
    }

    /**
     * инициализируйте плату
     * @param parent
     */
    void initBoard(Tetris parent) {

        setFocusable(true);
        setBorder(BorderFactory.createLineBorder(Color.pink, 4));
        timer = new Timer();
        timer.scheduleAtFixedRate(new ScheduleTask(),
                INITIAL_DELAY, PERIOD_INTERVAL);

        curPiece = new Shape();

        statusbar = parent.getStatusBar();
        board = new Tetrominoe[BOARD_WIDTH * BOARD_HEIGHT];
        addKeyListener(new TAdapter());
        clearBoard();
    }

    int squareWidth() {
        return (int) getSize().getWidth() / BOARD_WIDTH;
    }

    int squareHeight() {
        return (int) getSize().getHeight() / BOARD_HEIGHT;
    }

    Tetrominoe shapeAt(int x, int y) {
        return board[(y * BOARD_WIDTH) + x];
    }

    public void start() {

        isStarted = true;
        clearBoard();
        newPiece();
    }

    void pause() {

        if (!isStarted) {
            return;
        }

        isPaused = !isPaused;

        if (isPaused) {

            statusbar.setText("Paused");
        } else {

            statusbar.setText(String.valueOf(numLinesRemoved));
        }
    }

    // нарисуйте доску
    void doDrawing(Graphics g) {

        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight();

        for (int i = 0; i < BOARD_HEIGHT; ++i) {

            for (int j = 0; j < BOARD_WIDTH; ++j) {

                Tetrominoe shape = shapeAt(j, BOARD_HEIGHT - i - 1);

                if (shape != Tetrominoe.NoShape) {

                    drawSquare(g, 0 + j * squareWidth(),
                            boardTop + i * squareHeight(), shape);
                }
            }
        }

        if (curPiece.getShape() != Tetrominoe.NoShape) { // если форма изделия не является формой без формы

            for (int i = 0; i < 4; ++i) { // для количества строк в фигуре

                int x = curX + curPiece.x(i); // получите координату x элемента
                int y = curY - curPiece.y(i); // получите координату y детали
                drawSquare(g, 0 + x * squareWidth(),
                        boardTop + (BOARD_HEIGHT - y - 1) * squareHeight(),
                        curPiece.getShape()); // нарисуйте фигуру на доске
            }
        }
    }

    /**
     * нарисуйте квадрат на доске
     * @param g the <code>Graphics</code> object to protect
     */
    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        doDrawing(g);
    }

    /**
     * нарисуйте новые координаты фигуры при падении вниз
     */
    void dropDown() {

        int newY = curY; // установите новую координату y на текущую координату y

        while (newY > 0) { // в то время как новая координата y больше 0

            if (!tryMove(curPiece, curX, newY - 1)) { // если фигура не может переместиться в новые координаты

                break; // разорвать петлю
            }

            --newY; // уменьшите новую координату y
        }

        pieceDropped(); // вызовите метод удаления фрагмента
    }

    /**
     * нарисуйте новые координаты фигуры во время перемещения
     */
    void oneLineDown() {

        if (!tryMove(curPiece, curX, curY - 1)) {

            pieceDropped();
        }
    }

    void clearBoard() {

        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; ++i) {
            board[i] = Tetrominoe.NoShape;
        }
    }

    void pieceDropped() {

        for (int i = 0; i < 4; ++i) {

            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            board[(y * BOARD_WIDTH) + x] = curPiece.getShape();
        }

        removeFullLines(); // удалите полные строки

        if (!isFallingFinished) { // если кусок еще не закончил падать
            newPiece(); // создайте новый фрагмент
        }
    }

    /**
     * создайте новый фрагмент
     */
    void newPiece() {

        curPiece.setRandomShape(); // придайте изделию произвольную форму
        curX = BOARD_WIDTH / 2 + 1; // установите координату x фигуры на середину доски
        curY = BOARD_HEIGHT - 1 + curPiece.minY();

        if (!tryMove(curPiece, curX, curY)) { // если фигура не может переместиться в новые координаты
            curPiece.setShape(Tetrominoe.NoShape); // установите для фигуры значение "без формы"
            timer.cancel();
            isStarted = false;
            statusbar.setText("GAME OVER!");
        }
    }

    /**
     * Проверяет, может ли фигура переместиться в новые координаты
     * @param newPiece
     * @param newX
     * @param newY
     * @return
     */
    boolean tryMove(Shape newPiece, int newX, int newY) {

        for (int i = 0; i < 4; ++i) {

            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);

            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) { // если новые координаты отсутствуют на доске
                return false; // возвращает значение false
            }

            if (shapeAt(x, y) != Tetrominoe.NoShape) { // если новые координаты не являются формой no
                return false; // возвращает значение false
            }
        }

        curPiece = newPiece; // установите текущую часть на новую часть
        curX = newX;     // установите текущую координату x на новую координату x
        curY = newY;    // установите текущую координату y на новую координату y

        repaint(); // перекрасьте доску с новыми координатами фигуры

        return true; // возвращает значение true
    }

    /**
     * Удалите всю линию с доски
     */
    void removeFullLines() {

        int numFullLines = 0; // установите количество полных строк равным 0

        for (int i = BOARD_HEIGHT - 1; i >= 0; --i) { // для количества строк на доске
            boolean lineIsFull = true; // установите для параметра строка заполнена значение true

            for (int j = 0; j < BOARD_WIDTH; ++j) { // для количества столбцов на доске

                if (shapeAt(j, i) == Tetrominoe.NoShape) { // если фигура в координатах не имеет формы

                    lineIsFull = false; // установите для параметра строка заполнена значение false
                    break; // разорвать петлю
                }
            }

            if (lineIsFull) { // если строка заполнена

                ++numFullLines; // увеличьте количество полных строк

                for (int k = i; k < BOARD_HEIGHT - 1; ++k) { // для количества строк на доске
                    for (int j = 0; j < BOARD_WIDTH; ++j) { // для количества столбцов на доске

                        board[(k * BOARD_WIDTH) + j] = shapeAt(j, k + 1); // установите форму в координатах на форму в координатах, указанных ниже
                    }
                }
            }
        }

        if (numFullLines > 0) { // если количество полных строк больше 0

            numLinesRemoved += numFullLines; // увеличьте количество удаленных строк на количество полных строк
            statusbar.setText("Score: "+String.valueOf(numLinesRemoved)); // установите в тексте партитуры количество удаленных строк
            isFallingFinished = true; // установите, что фрагмент закончил падать до значения true
            curPiece.setShape(Tetrominoe.NoShape); // установите для фигуры значение "без формы"
            repaint();
        }
    }

    /**
     * нарисуйте квадрат на доске
     * @param g
     * @param x
     * @param y
     * @param shape
     */
    void drawSquare(Graphics g, int x, int y,
                    Tetrominoe shape) {

        Color colors[] = {
                new Color(0, 0, 0), new Color(204, 102, 102),
                new Color(102, 204, 102), new Color(102, 102, 204),
                new Color(204, 204, 102), new Color(204, 102, 204),
                new Color(102, 204, 204), new Color(218, 170, 0),

        };

        Color color = colors[shape.ordinal()];

        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);

        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);

        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1,
                x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1,
                x + squareWidth() - 1, y + 1);

    }

    void doGameCycle() {

        update();
        repaint();
    }

    void update() {

        if (isPaused) { // пауза в игре
            return; // вернуть
        }

        if (isFallingFinished) { // если фигура закончила падать

            isFallingFinished = false; // установите, что фигура закончила падать на false
            newPiece(); // создайте новый фрагмент
        } else {

            oneLineDown(); // переместите фигуру вниз на одну строку
        }
    }

    /**
     * class TAdapter-используется для проверки нажатых клавиш
     */
    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            if (!isStarted || curPiece.getShape() == Tetrominoe.NoShape) { // если игра не началась или фигура не имеет формы
                return; // вернуть
            }

            int keycode = e.getKeyCode(); // получите код нажатой клавиши

            if (keycode == KeyEvent.VK_ENTER) { // если нажатая клавиша является клавишей ввода
                pause(); // приостановите игру
                return; // вернуть
            }

            if (isPaused) {
                return;
            }

            switch (keycode) { // переключите код нажатой клавиши

                case KeyEvent.VK_LEFT: // если нажатой клавишей является стрелка влево
                    tryMove(curPiece, curX - 1, curY); // попробуйте передвинуть фигуру влево
                    break;

                case KeyEvent.VK_RIGHT: // если нажатой клавишей является стрелка вправо
                    tryMove(curPiece, curX + 1, curY); // попробуйте передвинуть фигуру вправо
                    break;

                case KeyEvent.VK_DOWN: // если нажатой клавишей является стрелка вниз
                    tryMove(curPiece.rotateRight(), curX, curY); // попробуйте повернуть фигурку вправо
                    break;
/**
 * если нажатой клавишей является стрелка вверх
 */
                case KeyEvent.VK_UP:
                    /**
                     *
                     попробуйте повернуть фигурку влево
                     */
                    tryMove(curPiece.rotateLeft(), curX, curY); //

                    break;

                case KeyEvent.VK_SPACE: // если нажатой клавишей является пробел
                    dropDown(); // опустите фигурку вниз
                    break;

                case KeyEvent.VK_D: // если нажатая клавиша является клавишей d
                    oneLineDown(); // переместите фигуру вниз на одну строку
                    break;
            }
        }
    }

    class ScheduleTask extends TimerTask {

        @Override
        public void run() {

            doGameCycle();
        }
    }
}

/**
 * основной класс игры
 */
class Tetris extends JFrame {

    static final long serialVersionUID = 1L;
    JLabel statusbar;

    public Tetris() {

        initUI();
    }

    void initUI() {

        JPanel panel = new JPanel();
        panel.setBackground(new Color(0XF5EBE0));

        statusbar = new JLabel("Score: 0");
        statusbar.setFont(new Font("MV Boli", Font.BOLD, 30));
        panel.add(statusbar, BorderLayout.NORTH);

        Board board = new Board(this);
        add(panel, BorderLayout.NORTH);
        add(board);
        board.setBackground(new Color(0Xf0e2d3));
        board.start();

        setTitle("Tetris");
        setSize(400, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    public JLabel getStatusBar() {

        return statusbar;
    }

    /**
     * запустите игру отсюда
     * @param args
     */
    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {

            Tetris game = new Tetris();
            game.setVisible(true);
        });
    }
}