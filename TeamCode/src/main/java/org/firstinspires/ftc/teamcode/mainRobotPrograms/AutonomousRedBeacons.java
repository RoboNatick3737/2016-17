package org.firstinspires.ftc.teamcode.mainRobotPrograms;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;

@Autonomous(name = "Autonomous - Red Beacons Edition", group = "Autonomous Group")
//@Disabled

public class AutonomousRedBeacons extends _AutonomousBase
{
    //Autonomous code for the Blue alliance

    //Called after runOpMode() has finished initializing.
    protected void driverStationSaysGO()
    {
        //Make color sensor available.
        leftSensorServo.setPosition(RIGHT_SERVO_OPEN);

        //Drive forward a bit
        driveForTime(-0.9, 600);
        outputNewLineToDriverStation("Went forward");

        //Turn to face between the lines
        setInitialTurnPower(.16);
        setPrecision(20);
        turnToHeading(-30, turnMode.BOTH);
        outputNewLineToDriverStation("Turned to face toward intermediary part of white line");
        int currentHeading = getValidGyroHeading();

        //Drive toward the wall.
        driveForTime(-0.9, 1600);
        outputNewLineToDriverStation("Driving forward toward that part.");

        //Turn to face the wall
        setPrecision(24);
        turnToHeading(-currentHeading - 90, turnMode.BOTH);
        outputNewLineToDriverStation("Turning to face wall.  ");

        //Drive to the wall until touch sensor registered.
        zeroHeading();
        setMovementPower(-0.5);
        while(touchSensor.isPressed() == false)
            idle();
        stopDriving();
        //The wall resets our heading (YES)
        zeroHeading();
        sleep(300);

        setInitialTurnPower(.2);
        setPrecision(5);
        turnToHeading(-93, turnMode.LEFT); //Back facing to near beacon

        driveForTime(0.2, 1400);

        //Drive to the first color line.
        zeroHeading();
        setMovementPower(-0.2);
        while (bottomColorSensor.alpha() < 10)
            updateMotorPowersBasedOnGyroHeading();
        stopDriving();
        adjustHeading();

        /********   BUTTON PUSHING SEQUENCE GOES HERE   ********/

        driveForTime(0.1, 500);

        if (leftColorSensor.red() >= 2 && leftColorSensor.blue() <= 2)
            pushButton();
        else
        {
            driveForTime(0.2, 450);
            pushButton();
        }

        outputNewLineToDriverStation("red is " + rightColorSensor.red() + " blue is " + rightColorSensor.blue());

        setPrecision(2);
        adjustHeading();

        /********        END BUTTON PUSH SEQUENCE       ********/

        //Drive a little ways forward from the first line
        driveForTime(-0.2, 800);

        //Drive to the second color line.
        zeroHeading();
        setMovementPower(-0.2);
        while (opModeIsActive() && bottomColorSensor.alpha() < 10)
            updateMotorPowersBasedOnGyroHeading();
        stopDriving();
        adjustHeading();

        /********   BUTTON PUSHING SEQUENCE GOES HERE   ********/

        driveForTime(0.1, 500);

        if (leftColorSensor.red() >= 2 && leftColorSensor.blue() <= 2)
            pushButton();
        else
        {
            driveForTime(0.2, 450);
            pushButton();
        }

        /********        END BUTTON PUSH SEQUENCE       ********/

    }

    //Should be constant among all classes.
    protected void pushButton()
    {
        //Press button
        pusher.setPower(-.12);
        sleep(1800);
        pusher.setPower(.12);
        sleep(1600);
        pusher.setPower(0);

    }
}