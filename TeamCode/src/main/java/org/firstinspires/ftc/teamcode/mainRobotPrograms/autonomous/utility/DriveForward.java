package org.firstinspires.ftc.teamcode.mainRobotPrograms.autonomous.utility;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.mainRobotPrograms.autonomous.AutonomousBase;

@Autonomous(name="Drive Forward", group = "Utility Group")

public class DriveForward extends AutonomousBase
{
    //Custom initialization
    @Override
    protected void driverStationSaysINITIALIZE() throws InterruptedException
    {
        //Set the motor powers.
        setLeftPower(-.6);
        setRightPower(.6);
    }

    //Called after runOpMode() has finished initializing.
    protected void driverStationSaysGO() throws InterruptedException
    {
        //Set the motor powers.
        startToDriveAt (0.8);

        while (true)
            idle();
    }
}