package org.makiah.niftc.threads;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

/**
 * This class is super important to running the program while making sure to check the user requested state of the program.  While running any wait method, the program will run essentially an idle() statement to check to see whether a stop was requested, and throw an InterruptedException in that event.
 */
public class NiFTFlow
{
    /**
     * NiFTBase calls this method at the start of the program's execution to allow it to check isStopRequested().
     */
    private static LinearOpMode mainLinOpMode;
    public static void initializeWith (LinearOpMode opMode)
    {
        mainLinOpMode = opMode;
    }

    /**
     * This method "waits" for a given number of seconds by running pauseForSingleFrame() as long as necessary.
     *
     * @param ms the milliseconds that the program should wait for.
     * @throws InterruptedException the exception which indicates that the program needs to stop.
     */
    public static void pauseForMS(long ms) throws InterruptedException
    {
        long startTime = System.currentTimeMillis ();
        while (System.currentTimeMillis () - startTime <= ms)
            pauseForSingleFrame ();
    }

    /**
     * This method quickly checks to see whether the program needs to stop before allowing other threads to run.
     *
     * @throws InterruptedException
     */
    public static void pauseForSingleFrame() throws InterruptedException
    {
        if (mainLinOpMode.isStopRequested())
            throw new InterruptedException();

        Thread.yield();
    }
}