package nervouschef;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
/**
 *
 * @author lailx
 */
public class MainFrame extends javax.swing.JFrame {

    private MenuPanel menuPanel;
    private GameOverPanel gameOverPanel;
    private GameScreen gameScreen;
    private PausePanel pausePanel;

    private java.awt.CardLayout cardLayout;

    private static final String MENU = "MENU";
    private static final String GAME_OVER = "GAME_OVER";
    private static final String GAME = "GAME";
    private static final String PAUSE = "PAUSE";
    
    // Initialize music (rayman origins dashing thru the fire)
    // normal volume (dB), muffled volume (dB), muffle cutoff (Hz)
    private final MusicPlayer music = new MusicPlayer("sounds/music.wav", -10f, -10f, 800);

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MainFrame.class.getName());

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();
        menuPanel = new MenuPanel();
        gameOverPanel = new GameOverPanel();
        gameScreen = new GameScreen();
        pausePanel = new PausePanel();
        cardLayout = (java.awt.CardLayout) cardPanel.getLayout();

        gameScreen.setScore(0);
        // display initial timer value
        gameScreen.setTimerSeconds(gameScreen.getSecondsRemaining());

        cardPanel.add(new BackgroundPanel("background.jpg", menuPanel), MENU);
        cardPanel.add(new BackgroundPanel("background.jpg", gameOverPanel), GAME_OVER);
        cardPanel.add(gameScreen, GAME);
        cardPanel.add(pausePanel, PAUSE);

        //handles start button by switching from menu screen to game screen
        menuPanel.setStartListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showGame();
            }
        });

        //handles the restart button by resetting and restarting the game.
        gameOverPanel.setRestartListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameScreen.reset();
                showGame();
            }
        });

        //handles the exit button
        gameOverPanel.setExitListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
                //closes app
            }
        });

        // handles resume button and returns to current game
        pausePanel.setResumeListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showGame();
            }
        });

        //handles the restart button by resetting and restarting the game.
        pausePanel.setRestartListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameScreen.reset();
                showGame();
            }
        });

        //handles the pause button 
        gameScreen.setPauseListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPause(); 
                //when the game is paused, end current session goes to gameoverpanel/screen
            }
        });

        // handles game timer reaching 0
        gameScreen.setTimerExpiredListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showGameOver(gameScreen.getScore()); // replace with actual score later(done)
                // actual score code
            }
        });

        //displays main menue when game starts
        showMenu();
        //sizes the window
        pack();
        //places window in center of screen
        setLocationRelativeTo(null);

    }

    //displays main menu and stops game loop
    public void showMenu() {
        music.setMuffled(true);
        music.play();
        
        gameScreen.stopGameLoop();
        cardLayout.show(cardPanel, MENU);

    }

    //displays game over screen, stops the current session and displays the final score
    public void showGameOver(int score) {
        music.setMuffled(true);
        
        gameScreen.stopGameLoop();
        gameOverPanel.setScore(score);
        cardLayout.show(cardPanel, GAME_OVER);
    }

    // switched to game screen and starts game loop
    public void showGame() {
        music.play();
        music.setMuffled(false);
        
        gameScreen.startGameLoop();
        cardLayout.show(cardPanel, GAME);
    }

    //disp pause screen, pauses current session and displays both buttons
    public void showPause() {
        music.setMuffled(true);
        
        gameScreen.stopGameLoop();
        cardLayout.show(cardPanel, PAUSE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cardPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new java.awt.CardLayout());

        cardPanel.setLayout(new java.awt.CardLayout());
        getContentPane().add(cardPanel, "card2");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cardPanel;
    // End of variables declaration//GEN-END:variables

}
