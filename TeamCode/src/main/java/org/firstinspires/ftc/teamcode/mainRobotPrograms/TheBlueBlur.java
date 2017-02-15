package org.firstinspires.ftc.teamcode.mainRobotPrograms;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.util.Range;

@Autonomous(name="The Blue Blur", group = "Beta Group")

public class TheBlueBlur extends _AutonomousBase
{

    //Called after runOpMode() has finished initializing by BaseFunctions.
    protected void driverStationSaysGO() throws InterruptedException
    {
        //Drive until we are just far enough from the cap ball to score reliably.
        outputNewLineToDrivers("Driving forward to the cap ball to score...");
        setMovementPower(.3);
        while(frontRangeSensor.cmUltrasonic() > 40)
            adjustMotorPowersBasedOnGyroSensor();
        stopDriving();

        //Shoot the balls into the center vortex.
        outputNewLineToDrivers("Shooting balls into center vortex...");
        flywheels.setPower(0.4);
        sleep(300);
        harvester.setPower(1.0);
        sleep(3000);
        flywheels.setPower(0);
        harvester.setPower(0);

        //Drive a bit further forward toward the cap ball so that when we turn we don't end up crashing into the corner vortex.
        outputNewLineToDrivers("Driving forward toward the cap ball before turn...");
        setMovementPower(.4);
        while(frontRangeSensor.cmUltrasonic() > 20)
            adjustMotorPowersBasedOnGyroSensor();
        stopDriving();

        //Turn to face the wall directly.
        outputNewLineToDrivers("Turning to face wall...");
        turnToHeading(90, TurnMode.BOTH, 4000);

        //Drive to the wall and stop once a little ways away.
        setMovementPower (0.4);
        outputNewLineToDrivers("Driving to wall before turn...");
        while (frontRangeSensor.cmUltrasonic() > 60)
            adjustMotorPowersBasedOnGyroSensor();
        setMovementPower (0.25);
        while (frontRangeSensor.cmUltrasonic () > 46)
            adjustMotorPowersBasedOnGyroSensor ();
        stopDriving ();

        //Turn back to become parallel with the wall.
        outputNewLineToDrivers("Turning to become parallel to the wall...");
        turnToHeading(0, TurnMode.RIGHT, 5000);

        //For each of the two beacons.
        for (int i = 0; i < 2; i++)
        {
            outputNewLineToDrivers ("Doing beacon " + (i + 1));
            outputNewLineToDrivers ("Looking for the blue beacon...");

            //Set movement speed.
            setMovementPower(0.25);

            //Variables required for range sensor adjustment.
            long lastAdjustTime = System.currentTimeMillis ();
            double idealDistance = 10;

            //Changed as the loop below progresses.
            boolean option1Red = false, option1Blue = false, option2Red = false, option2Blue = false;

            //Drive until centered on the beacon.
            while (! ((option1Blue && option2Red) || (option1Red && option2Blue)))
            {
                //Adjust gyro based on range sensor.
                double currentDist = sideRangeSensor.cmUltrasonic ();
                if (Math.abs (currentDist - idealDistance) >= 2)
                {
                    if (System.currentTimeMillis () - lastAdjustTime > 1000)
                    {
                        gyroAdjustFactor -= Math.signum (currentDist - idealDistance);
                        lastAdjustTime = System.currentTimeMillis ();
                        outputNewLineToDrivers ("Adjusted gyro factor, too far or close to wall, now is " + gyroAdjustFactor);
                    }
                }

                //Adjust motors based on gyro to remain parallel to wall.
                adjustMotorPowersBasedOnGyroSensor ();

                //Check both sensors and adjust booleans accordingly.
                option1Red = option1ColorSensor.red () >= 2;
                option1Blue = option1ColorSensor.blue () >= 2;
                option2Red = option2ColorSensor.red () >= 2;
                option2Blue = option2ColorSensor.blue () >= 2;
            }

            //Stop driving when centered.
            stopDriving ();
            outputNewLineToDrivers ("Ahoy there!  Beacon spotted!  Option 1 is " + (option1Blue ? "blue" : "red") + " and option 2 is " + (option2Blue ? "blue" : "red"));

            //Try to turn as close to parallel to the wall as possible, since we occasionally are a bit off course when we arrive.
            turnToHeading (0, TurnMode.BOTH, 1500);

            //While the beacon is not completely blue (this is the verification step).
            int trials = 1; //The robot tries different drives for each trial.
            while (! (option1Blue && option2Blue))
            {
                outputNewLineToDrivers ("Beacon is not completely blue, attempting to press the correct color!");

                //Based on this logic, the correct button should always be pressed.  The trial also affects the distance that the robot drives, since if the button is not pressed somehow, then something new happens.
                if (option1Blue)
                {
                    outputNewLineToDrivers ("Chose option 1");
                    //Use the option 1 button pusher.
                    driveForDistance (0.25, (int) (50 * (Math.log(trials) + 1)));
                    pressButton();
                    driveForDistance (-0.25, (int) (50 * (Math.log(trials) + 1))); //Drive a ways forward before looking for it again.
                }
                else if (option2Blue)
                {
                    outputNewLineToDrivers ("Chose option 2");
                    //Use the option 2 button pusher.
                    driveForDistance (-0.25, (int) (80 * (Math.log(trials) + 1)));
                    pressButton();
                    driveForDistance (0.25, (int) (80 * (Math.log(trials) + 1)));
                }
                else if (option1Red && option2Red)
                {
                    outputNewLineToDrivers ("Neither option is blue, toggling beacon!");
                    //Toggle beacon.
                    driveForDistance (0.25, (int) (50 * (Math.log(trials) + 1)));
                    pressButton();
                    driveForDistance (-0.25, (int) (50 * (Math.log(trials) + 1)));
                }

                idle();

                //Update beacon states to check loop condition.
                option1Red = option1ColorSensor.red () >= 2;
                option1Blue = option1ColorSensor.blue () >= 2;
                option2Red = option2ColorSensor.red () >= 2;
                option2Blue = option2ColorSensor.blue () >= 2;

                //Update the number of trials completed so that we know the new drive distance and such.
                trials++;
            }

            outputNewLineToDrivers ("Success!  Beacon is completely blue.");

            //Drive a bit forward from the white line to set up for the next step.
            driveForDistance (0.3, 300);
        }

        //Dash backward to the ramp afterward.

    }

    private void pressButton() throws InterruptedException
    {
        //Determine the length to push the pusher out based on the distance from the wall.
        double extendLength = 100 * sideRangeSensor.cmUltrasonic();
        extendLength = Range.clip(extendLength, 0, 3000);
        outputNewLineToDrivers ("Extending the button pusher for " + extendLength + " ms.");

        //Run the continuous rotation servo out to press, then back in.
        rightButtonPusher.setPosition(.2);
        sleep((long) (extendLength));
        rightButtonPusher.setPosition(.8);
        sleep((long) (extendLength));
        rightButtonPusher.setPosition(.5);
    }
}