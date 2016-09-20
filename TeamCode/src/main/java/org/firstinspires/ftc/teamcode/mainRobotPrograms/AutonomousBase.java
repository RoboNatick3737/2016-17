package org.firstinspires.ftc.teamcode.mainRobotPrograms;

import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.util.Range;

//For added simplicity while coding autonomous with the new FTC system. Utilized inheritance and polymorphism.
public abstract class AutonomousBase extends RobotBase {

    //Only used during autonomous.
    protected GyroSensor gyroscope;

    @Override
    protected void customInitialization() throws InterruptedException
    {
        try
        {
            //Get the gyroscope
            gyroscope = hardwareMap.gyroSensor.get("Gyroscope");
            gyroscope.calibrate();
            sleep(1000);
            // Make sure gyroscope is calibrated
            while (gyroscope.isCalibrating())
            {
                telemetry.addData("Gyroscope Calibrating", "");
                sleep(50);
            }
            telemetry.addData("Gyroscope Calibration Complete", "");
        } catch (Exception e) {
            telemetry.addData("Error in Gyroscope Calibration!", "");
            gyroscope = null;
        }
    }

    //All children should have special instructions.
    protected abstract void runInstructions() throws InterruptedException;

    private final double MIN_TURN_POWER = .3, MAX_TURN_POWER = 1;

    //Used to turn to a specified heading.
    protected void turn(double power, double heading) throws InterruptedException
    {
        if (gyroscope != null)
        {
            //Output a message to the user.
            telemetry.addData("Initiating turn with gyroscope...", "");

            //Wait a moment: otherwise there tends to an error.
            sleep(500);
            gyroscope.resetZAxisIntegrator();

            //The variables used to calculate the way that the motors will turn.
            int currHeading;
            double leftPower, rightPower;

            while (getGyroscopeHeading() != heading) {
                //Get the gyroscope heading.
                currHeading = getGyroscopeHeading();

                //Calculate the power of each respective motor.
                leftPower = (currHeading - heading) * power;
                rightPower = (heading - currHeading) * power;

                //Clamp values.
                if (leftPower >= 0)
                    leftPower = Range.clip(leftPower, MIN_TURN_POWER, MAX_TURN_POWER);
                else if (leftPower < 0)
                    leftPower = Range.clip(leftPower, -MAX_TURN_POWER, -MIN_TURN_POWER);

                if (rightPower >= 0)
                    rightPower = Range.clip(rightPower, MIN_TURN_POWER, MAX_TURN_POWER);
                else if (rightPower < 0)
                    rightPower = Range.clip(rightPower, -MAX_TURN_POWER, -MIN_TURN_POWER);

                //Set the motor powers.
                frontLeft.setPower(leftPower);
                backLeft.setPower(leftPower);
                frontRight.setPower(rightPower);
                backRight.setPower(rightPower);

                //Output data to the DS.
                telemetry.addData("Current heading ", currHeading);
                telemetry.addData("Turning to ", heading);
                telemetry.addData("Left Power ", leftPower);
                telemetry.addData("Right Power ", rightPower);

                idle();
            }
        } else {
            //Important for the driver to know.
            telemetry.addData("Initiating turn WITHOUT GYRO", "");

            //The turning point.
            frontLeft.setPower(power);
            backLeft.setPower(power);
            frontRight.setPower(power);
            backRight.setPower(power);

            //Sleep for some period of time.
            sleep((int) (heading));
            telemetry.addData("Turn complete!", "");
        }

        stopMotors();

        sleep(1000);
    }

    //Used to drive in a straight line with the aid of the gyroscope.
    protected void drive(double power, double length) throws InterruptedException
    {
        telemetry.addData("Driving...", "");

        //Initialize the gyro if it exists.
        if (gyroscope != null)
            gyroscope.resetZAxisIntegrator();

        //Required variables.
        double startTime = System.currentTimeMillis();
        int heading;

        //Set initial motor powers.
        frontLeft.setPower(power);
        backLeft.setPower(power);
        frontRight.setPower(power);
        backRight.setPower(power);

        sleep(500);

        while (System.currentTimeMillis() - startTime < length) {
            if (gyroscope != null)
            {
                // Get the heading info.
                heading = getGyroscopeHeading();

                //The gyroscope heading value has to be translated into a useful value.  It currently goes to 359 and then moves down when moving clockwise, and goes up from 0 at moving counter-clockwise.

                //Create values.
                double leftPower = power + (heading) / (20.0);
                double rightPower = power - (heading) / (20.0);

                //Clamp values.
                if (leftPower > 1)
                    leftPower = 1;
                else if (leftPower < -1)
                    leftPower = -1;

                if (rightPower > 1)
                    rightPower = 1;
                else if (rightPower < -1)
                    rightPower = -1;

                //Set the motor powers.
                frontLeft.setPower(leftPower);
                backLeft.setPower(leftPower);
                frontRight.setPower(rightPower);
                backRight.setPower(rightPower);

                //Output data to the DS.
                //Note: the 2nd parameter "%.2f" changes the output of the max decimal points.
                telemetry.addData("Heading: ", heading);
                telemetry.addData("Current Time: ", (System.currentTimeMillis() - startTime) * 1);
                telemetry.addData("Stopping at: ", length);
                telemetry.addData("L Power: ", leftPower);
                telemetry.addData("R Power: ", rightPower);
            } else {
                //Update the drivers.
                telemetry.addData("Running WITHOUT gyro (none initialized)", "");
                //The code should handle stopping and the rest.
            }

            //Wait a hardware cycle.
            idle();
        }
        //Stop the bot.
        stopMotors();

        sleep(1000);
    }

    //The gyroscope value goes from 0 to 360: when the bot turns left, it immediately goes to 360.  This makes sure that the value makes sense for calculations.
    protected int getGyroscopeHeading() {
        int heading = gyroscope.getHeading();

        if (heading > 180 && heading < 360)
            heading -= 360;

        return heading;
    }

    //Stops all drive motors.
    protected void stopMotors() {
        backLeft.setPower(0);
        frontLeft.setPower(0);
        backRight.setPower(0);
        frontRight.setPower(0);
    }
}