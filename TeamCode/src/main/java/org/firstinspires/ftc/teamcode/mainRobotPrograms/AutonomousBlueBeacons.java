package org.firstinspires.ftc.teamcode.mainRobotPrograms;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Autonomous - Blue Beacons Edition", group = "Autonomous Group")
//@Disabled

public class AutonomousBlueBeacons extends _AutonomousBase
{
    //Autonomous code for the Blue alliance

    //Called after runOpMode() has finished initializing.
    protected void driverStationSaysGO()
    {
        rightSensorServo.setPosition(RIGHT_SERVO_MAX);
        sleep(0);
        driveForTime(0.5, 200);   // Drive forward a little ways from the wall.
        turn(.6, -40);       // Turn 40 degrees.

        //Drive to the color sensor.
        zeroHeading();
        setMovementPower(0.5);
        while (bottomColorSensor.alpha() < 10)
            updateMotorPowersBasedOnGyroHeading();

        stopDriving();
    }
}