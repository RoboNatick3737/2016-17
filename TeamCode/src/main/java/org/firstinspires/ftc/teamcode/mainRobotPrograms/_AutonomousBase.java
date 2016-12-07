package org.firstinspires.ftc.teamcode.mainRobotPrograms;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.BatteryChecker;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.hardware.I2cAddr;

//For added simplicity while coding autonomous with the new FTC system. Utilized inheritance and polymorphism.
public abstract class _AutonomousBase extends _RobotBase
{
    //Only used during autonomous.
    protected GyroSensor gyroscope;
    protected ColorSensor leftColorSensor, rightColorSensor, bottomColorSensor; //Must have different I2C addresses.
    protected Servo leftSensorServo, rightSensorServo;
    protected final double RIGHT_SERVO_CLOSED = 1.0, LEFT_SERVO_CLOSED = 1.0;
    protected final double LEFT_SERVO_OPEN = 0.48, RIGHT_SERVO_OPEN = 0.48;

    // Initialize everything required in autonomous that isn't initialized in RobotBase (sensors)
    @Override
    protected void driverStationSaysINITIALIZE()
    {
        //initialize color sensors for either side (do in _AutonomousBase because they are useless during teleop.
        leftColorSensor = initialize(ColorSensor.class, "colorLeft");
        leftColorSensor.setI2cAddress(I2cAddr.create8bit(0x2c));
        leftColorSensor.enableLed(true);
//        rightColorSensor = initialize(ColorSensor.class, "colorRight");
//        rightColorSensor.setI2cAddress(I2cAddr.create8bit(0x3c));
//        rightColorSensor.enableLed(true);
        bottomColorSensor = initialize(ColorSensor.class, "colorBottom");
        bottomColorSensor.setI2cAddress(I2cAddr.create8bit(0x4c));
        bottomColorSensor.enableLed(true);

        leftSensorServo = initialize(Servo.class, "servoLeft");
        leftSensorServo.setPosition(LEFT_SERVO_CLOSED);
        rightSensorServo = initialize(Servo.class, "servoRight");
        rightSensorServo.setPosition(RIGHT_SERVO_CLOSED);

        //initialize gyroscope (will output whether it was found or not.
        gyroscope = initialize(GyroSensor.class, "Gyroscope");
        if (gyroscope != null)
        {
            //Start gyroscope calibration.
            outputNewLineToDriverStation("Gyroscope Calibrating...");
            gyroscope.calibrate();

            //Pause to prevent errors.
            sleep(1000);

            while (opModeIsActive() && gyroscope.isCalibrating())
                sleep(50);

            outputNewLineToDriverStation("Gyroscope Calibration Complete!");
        }

    }

    //All children should have special instructions.
    protected abstract void driverStationSaysGO();

    //Used to set drive move power initially.
    protected double movementPower = .5f;
    protected void setMovementPower(double movementPower)
    {
        this.movementPower = movementPower;

        for (DcMotor lMotor: leftDriveMotors)
            lMotor.setPower(movementPower);
        for (DcMotor rMotor : rightDriveMotors)
            rMotor.setPower(movementPower);
    }

    //Just resets the gyro.
    protected void zeroHeading()
    {
        sleep(500);
        gyroscope.resetZAxisIntegrator();
        sleep(500);
    }

    //More complex method that adjusts the heading based on the gyro heading.
    protected double offCourseSensitivity = 50; //Max of 100, Min of 0 (DON'T DO 100 OR DIV BY 0 ERROR)
    protected void updateMotorPowersBasedOnGyroHeading()
    {
        if (gyroscope != null)
        {
            // Get the heading info.
            int heading = getValidGyroHeading();

            //Create values.
            double leftPower = movementPower + (heading) / (100.0 - offCourseSensitivity);
            double rightPower = movementPower - (heading) / (100.0 - offCourseSensitivity);

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
            for (DcMotor lMotor : leftDriveMotors)
                lMotor.setPower(leftPower);
            for (DcMotor rMotor : rightDriveMotors)
                rMotor.setPower(rightPower);

            //Output data to the DS.
            //Note: the 2nd parameter "%.2f" changes the output of the max decimal points.
            outputConstantLinesToDriverStation(
                    new String[] {
                            "Heading: " + heading,
                            "L Power: " + leftPower,
                            "R Power: " + rightPower
                    }
            );
        } else
        {
            outputConstantLinesToDriverStation(
                    new String[] {
                            "Can't adjust heading, no gyro attached!"
                    }
            );
        }

        idle();
    }

    //Used to turn to a specified heading.
    private final double MIN_TURN_POWER = .1, MAX_TURN_POWER = .5;
    protected void turn (double heading)
    {
        if (gyroscope != null)
        {
            //Output a message to the user
            outputNewLineToDriverStation("Turning to " + heading + " degrees WITH GYRO");

            //Wait a moment: otherwise there tends to an error.
            zeroHeading();

            //The variables used to calculate the way that the motors will turn.
            int currHeading;
            double leftPower, rightPower;

            while (opModeIsActive() && getValidGyroHeading() != heading)
            {
                //Get the gyroscope heading.
                currHeading = getValidGyroHeading();

                //Calculate the power of each respective motor.
                double differenceInHeadings = heading - currHeading;
                double power = 0.10 * Math.pow(differenceInHeadings, 2) + MIN_TURN_POWER;
                leftPower = -power;
                rightPower = power;

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
                for (DcMotor lMotor: leftDriveMotors)
                    lMotor.setPower(leftPower);
                for (DcMotor rMotor : rightDriveMotors)
                    rMotor.setPower(rightPower);

                //Output data to the DS.
                //Don't change this, since it is useful to have real-time data in this case.
                outputConstantLinesToDriverStation(
                        new String[] {
                                "Turning with gyro...",
                                "Current heading " + currHeading,
                                "Turning to " + heading,
                                "Left Power " + leftPower,
                                "Right Power " + rightPower
                        }
                );

                idle();
            }
        } else {
            int coefficient = heading < 0 ? -1 : 1;
            //The turning point..
            for (DcMotor lMotor: leftDriveMotors)
                lMotor.setPower(heading * .3);
            for (DcMotor rMotor : rightDriveMotors)
                rMotor.setPower(heading * -.3);

            //Sleep for some period of time.
            sleep((int) (heading));
        }

        stopDriving();

        outputNewLineToDriverStation("Turn completed.");
    }

    //Used to driveForTime in a straight line with the aid of the gyroscope.
    protected void driveForTime(double power, long length)
    {
        if (gyroscope != null)
        {
            //Add the output to the driver station.
            outputNewLineToDriverStation("Driving at " + power + " power, for " + length + " milliseconds, with a gyroscope");

            zeroHeading(); // Set the direction to move.

            setMovementPower(power); // Set the initial power.

            //Required variables.
            double startTime = System.currentTimeMillis();

            sleep(500);

            //Gyroscope turning mechanics.
            while (opModeIsActive() && System.currentTimeMillis() - startTime < length)
                updateMotorPowersBasedOnGyroHeading();

            //Stop the bot.
            stopDriving();

            outputNewLineToDriverStation("Drove for " + length + " at " + power + ".");
        }
        else
        {
            setMovementPower(power); // Set the initial power.
            outputNewLineToDriverStation("Driving at " + power + " power, for " + length + " milliseconds, without a gyroscope");
            sleep(length);
        }
    }

    //The gyroscope value goes from 0 to 360: when the bot turns left, it immediately goes to 360.  This makes sure that the value makes sense for calculations.
    protected int getValidGyroHeading()
    {
        int heading = gyroscope.getHeading();

        if (heading > 180 && heading < 360)
            heading -= 360;

        return heading;
    }

    //Stops all driveForTime motors.
    protected void stopDriving ()
    {
        for (DcMotor lMotor: leftDriveMotors)
            lMotor.setPower(0);
        for (DcMotor rMotor : rightDriveMotors)
            rMotor.setPower(0);
    }
}
