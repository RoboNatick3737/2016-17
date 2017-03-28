package org.firstinspires.ftc.teamcode.debugging.programs;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.MainRobotBase;
import org.firstinspires.ftc.teamcode.autonomous.AutoBase;

@Autonomous(name="Drive Forward", group = "Utility Group")

public class DriveForward extends MainRobotBase
{
    @Override
    protected void driverStationSaysINITIALIZE()
    {
        //Set the motor powers.
        leftDrive.setDirectMotorPower (.5);
        rightDrive.setDirectMotorPower (-.5);
    }

    protected void driverStationSaysGO() throws InterruptedException
    {
        //Set the motor powers.
        leftDrive.setDirectMotorPower (.8);
        rightDrive.setDirectMotorPower (.8);

        while (true)
            idle();
    }
}