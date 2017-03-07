package org.firstinspires.ftc.teamcode.mainRobotPrograms;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.util.Range;

@Autonomous(name="Drive Testing", group = "Test Group")

public class DriveTesting extends _AutonomousBase
{
    //Called after runOpMode() has finished initializing by BaseFunctions.
    protected void driverStationSaysGO() throws InterruptedException
    {
        int sign = 1;
        while (opModeIsActive ())
        {
            driveForDistance (sign * Math.random () * 0.7 + 0.3, 2000);
            sign *= -1;
        }
    }
}