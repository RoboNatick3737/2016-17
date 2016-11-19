package org.firstinspires.ftc.teamcode.mainRobotPrograms;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

@Autonomous(name = "Autonomous - Blue Edition", group = "Autonomous Group")
//@Disabled

public class AutonomousBlue extends AutonomousBase {
    //Autonomous code for the Blue alliance

    //Called after runOpMode() has finished initializing.
    protected void driverStationSaysGO() throws InterruptedException
    {
        sleep(0);
        drive(80, 1200);
        turn(0.3, -110);
        drive(80, 2000);
        harvester.setPower(0.5);
        sleep(3000);
    }
}