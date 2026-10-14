/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nervouschef;

/**
 *
 * @author lailx
 */
public class CountdownTimer {

    private final long durationMs;
    private long timeRemainingMs;
    private boolean finished;

    public CountdownTimer(long durationMs) {
        this.durationMs = durationMs;
        this.timeRemainingMs = durationMs;
        this.finished = false;
    }

    // reduces the timer by the elapsed time 
    public void update(long elapsedMs) {

        // Ignore invalid elapsed times
        if (elapsedMs <= 0) {
            return;
        }

        if (finished) {
            return;
        }

        timeRemainingMs -= elapsedMs;

        if (timeRemainingMs <= 0) {
            timeRemainingMs = 0;
            finished = true;
        }
    }

    // resets timer to its starting duration
    public void reset() {
        timeRemainingMs = durationMs;
        finished = false;
    }

    // returns seconds remaining, rounded up
    public int getSecondsRemaining() {
        return (int) Math.ceil(timeRemainingMs / 1000.0);
    }

    // returns true when timer reaches zero
    public boolean isFinished() {
        return finished;
    }
}
