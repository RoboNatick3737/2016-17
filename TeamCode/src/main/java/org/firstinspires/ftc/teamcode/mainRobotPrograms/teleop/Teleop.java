package org.firstinspires.ftc.teamcode.mainRobotPrograms.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.mainRobotPrograms.RobotBase;

//Add the teleop to the op mode register.
@TeleOp(name="Teleop", group="Teleop Group")

public class Teleop extends RobotBase
{
    //All teleop controls are here.
    protected void driverStationSaysGO() throws InterruptedException
    {
        //Normal mode variables
        double leftPower,
                rightPower,
                leftPusherPower,
                rightPusherPower;
        boolean backwards = false;
        double speedCoefficient = 1.0;
        double flywheelCoefficient = 0.4;
        boolean pressingFlywheelC = false;
        double currentCapBallHolderPos = 0;
        boolean capBallMode = false;
        boolean capBallMode2 = false;
        double harvesterCoefficient = 1.0;

        //Keep looping while opmode is active (waiting a hardware cycle after all of this is completed, just like loop())
        while (true)
        {
            /**************************** CONTROLLER #1 ********************************/
            /************** Direction Toggle **************/
            if (!backwards)
            { // Driving forward
                leftPower = -gamepad1.left_stick_y;
                rightPower = -gamepad1.right_stick_y;
            } else { // Driving backward
                leftPower = gamepad1.right_stick_y;
                rightPower = gamepad1.left_stick_y;
            }

            /************** Motor Speed Control **************/
            rightPower = Range.clip(rightPower, -1, 1);
            leftPower = Range.clip(leftPower, -1, 1);

            // Write the values to the motors.  Scale the robot in order to run the robot more effectively at slower speeds.
            setLeftPower(scaleInput(leftPower) * speedCoefficient);
            setRightPower(scaleInput(rightPower) * speedCoefficient);

            /************** Cap Ball Drive Mode **************/
            if (gamepad1.x) {
                speedCoefficient = 0.5;
//                backwards = true;
                capBallMode = true;
            }
            else if (gamepad1.y) {
                speedCoefficient = 1.0;
//                backwards = false;
                capBallMode = false;
            }

            if (gamepad1.right_bumper) {
                speedCoefficient = 0.5;
            }
            else if (!capBallMode) {
                speedCoefficient = 1.0;
            }

            /************** Clamp **************/
            if (gamepad1.left_bumper || (gamepad2.left_bumper && capBallMode2))
                currentCapBallHolderPos += 0.05;
            else if (gamepad1.left_trigger > 0.5 || (gamepad2.left_trigger > 0.5 && capBallMode2))
                currentCapBallHolderPos -= 0.01;
            currentCapBallHolderPos = Range.clip(currentCapBallHolderPos, CBH_CLOSED, CBH_OPEN);
            capBallHolder.setPosition(currentCapBallHolderPos);

            /************** Open Clamp **************/
            if (gamepad1.back || gamepad2.back) {
                capBallHolder.setPosition(CBH_OPEN);
            }

            /**************************** CONTROLLER #2 ********************************/
            /************** Cap Ball Lift **************/
            if (gamepad2.x) {
                capBallMode2 = true;
            }
            else if (gamepad2.y) {
                capBallMode2 = false;
            }

            if (gamepad2.right_bumper)
                lift.setPower(1.0);
            else if (gamepad2.right_trigger > 0.5)
                lift.setPower(-0.5);
            else
                lift.setPower(0.0);

            /************** Harvester **************/
            if (gamepad2.b) {
                harvester.setPower(harvesterCoefficient * 1.0); // Collect
            }
            else if (gamepad2.a && gamepad2.dpad_down) {
                harvester.setPower(harvesterCoefficient * -1.0);
                flywheels.setPower(-1.0);
            }
            else if (gamepad2.a) {
                harvester.setPower(harvesterCoefficient * -1.0); // Reverse harvester
            }
            else {
                harvester.setPower(0);
            }

            /************** Flywheels **************/
            if (gamepad2.dpad_up) {
                flywheels.setPower(flywheelCoefficient * 1.0); // Shoot
                harvesterCoefficient = 1.0;
            }
            else if (gamepad2.b && gamepad2.dpad_down) {
                harvester.setPower(harvesterCoefficient * -1.0);
                flywheels.setPower(-1.0);
            }
            else if (gamepad2.dpad_down) {
                flywheels.setPower(-0.6); // Reverse flywheels
                harvesterCoefficient = 1.0;
            }
            else {
                flywheels.setPower(0.0);
                harvesterCoefficient = 1.0;
            }

            //Used to adjust the power of the flywheels.
            if (gamepad2.left_trigger > 0.5 && !pressingFlywheelC && !capBallMode) {
                flywheelCoefficient = Range.clip(flywheelCoefficient - 0.05, 0.2, 1.0);
                pressingFlywheelC = true;
            }
            else if (gamepad2.left_bumper && !pressingFlywheelC && !capBallMode) {
                flywheelCoefficient = Range.clip(flywheelCoefficient + 0.05, 0.2, 1.0);
                pressingFlywheelC = true;
            }

            if (!(gamepad2.left_trigger > 0.5) && !gamepad2.left_bumper) {
                pressingFlywheelC = false;
            }

            /************** Beacon Pushers **************/
            leftPusherPower = gamepad2.right_stick_x;
            leftPusherPower = Range.clip(leftPusherPower, -1, 1);

            if (leftPusherPower > 0.5)
                leftButtonPusher.setPosition(1.0);
            else if (leftPusherPower < -0.5)
                leftButtonPusher.setPosition(0.0);
            else
                leftButtonPusher.setPosition(0.5);

            rightPusherPower = gamepad2.left_stick_x;
            rightPusherPower = Range.clip(rightPusherPower, -1, 1);

            if (rightPusherPower > 0.5)
                rightButtonPusher.setPosition(1.0);
            else if (rightPusherPower < -0.5)
                rightButtonPusher.setPosition(0.0);
            else
                rightButtonPusher.setPosition(0.5);

            /************** Data Output **************/
            outputConstantDataToDrivers(new String[] {
                    "Fly wheel power = " + flywheelCoefficient,
                    "Drive power = " + speedCoefficient,
                    "Cap ball mode = " + capBallMode,
                    "Distance from the wall = " + sideRangeSensor.cmUltrasonic ()
            });

            idle();

            /******************** END OF LOOP ********************/
        }
    }

    /*
     * This method scales the joystick input so for low joystick values, the
     * scaled value is less than linear.  This is to make it easier to drive
     * the robot more precisely at slower speeds.
     */

    double scaleInput(double dVal)
    {
        double[] scaleArray = {0.0, 0.05, 0.09, 0.10, 0.12, 0.15, 0.18, 0.24,
                0.30, 0.36, 0.43, 0.50, 0.60, 0.72, 0.85, 1.00, 1.00};

        // get the corresponding index for the scaleInput array.
        int index = (int) (dVal * 16.0);
        if (index < 0) {
            index = -index;
        } else if (index > 16) {
            index = 16;
        }

        double dScale = 0.0;
        if (dVal < 0) {
            dScale = -scaleArray[index];
        } else {
            dScale = scaleArray[index];
        }

        return dScale;
    }

}