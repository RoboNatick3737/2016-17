package org.firstinspires.ftc.teamcode.niftc.hardware;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.niftc.threads.NiFTFlow;
import org.firstinspires.ftc.teamcode.niftc.threads.NiFTSimpleTask;
import org.firstinspires.ftc.teamcode.niftc.threads.NiFTTaskPackage;

/**
 * Accurate and helpful PID is a difficult feat to accomplish, this a simplistic approach which works fairly well and is highly customizable.
 *
 * Advantages:
 * - Accurate motor power supplied right off the bat which works well with gyro adjustment.
 * - Simplistic and easy to follow.
 * - Requires easy-to-visualize calculations and can update at variable rates.
 */
public class NiFTMotorController
{
    /**
     * The package which contains all PID tasks (run sequentially in one complex task instance.
     */
    public static NiFTTaskPackage pidTaskPackage;

    /**
     * Most encoded motor sets have one motor, but this can often vary.
     */
    public final String name;
    public final DcMotor encoderMotor;
    public final DcMotor[] linkedMotors;

    /**
     * This constructor enables a variable number of linked motors to be specified easily, with a single motor that has an encoder attached.  This is ideal for drive motors, but can be also used with single motor systems (see constructor below)
     *
     * @param name
     * @param encoderMotorName
     * @param linkedMotorNames
     */
    public NiFTMotorController (String name, String encoderMotorName, String... linkedMotorNames)
    {
        this.name = name;

        this.encoderMotor = NiFTInitializer.initialize (DcMotor.class, encoderMotorName);

        //Initialize all linked motors.
        if (linkedMotorNames != null)
        {
            linkedMotors = new DcMotor[linkedMotorNames.length];
            for (int i = 0; i < linkedMotorNames.length; i++)
                linkedMotors[i] = NiFTInitializer.initialize (DcMotor.class, linkedMotorNames[i]);
        } else
            linkedMotors = null;

        pidInstance = new PIDTask ();

        resetEncoder ();
    }
    public NiFTMotorController (String name, String encoderMotorName)
    {
        this (name, encoderMotorName, null);
    }

    /**
     * The RPS conversion factor is a variable which houses the conversion factor from power to revolutions per second.  The motor should spin at a constant rate, but there are often different factors which prevent this from happening all the time, namely friction and stress.  This coefficient embodies the way that this fact is combatted.
     */
    private double rpsConversionFactor = .25;
    public double getRPSConversionFactor () //Primarily for debugging.
    {
        return rpsConversionFactor;
    }
    public NiFTMotorController setRPSConversionFactor (double rpsConversionFactor)
    {
        this.rpsConversionFactor = rpsConversionFactor;
        return this;
    }

    /**
     * Different motors have different encoder ticks per wheel revolution.  Set this to make it rotate accurately based on your motor type.
     */
    public enum MotorType
    {
        NeverRest40 (1120), NeverRest20 (1120), NeverRest3P7 (45);

        public final int encoderTicksPerRevolution;

        MotorType (int encoderTicksPerRevolution)
        {
            this.encoderTicksPerRevolution = encoderTicksPerRevolution;
        }
    }

    private MotorType motorType = MotorType.NeverRest40;

    public NiFTMotorController setMotorType (MotorType motorType)
    {
        this.motorType = motorType;
        return this;
    }

    /**
     * Sets all motors to the direction specified.
     *
     * @param direction
     */
    public NiFTMotorController setMotorDirection (DcMotorSimple.Direction direction)
    {
        encoderMotor.setDirection (direction);
        if (linkedMotors != null)
            for (DcMotor linkedMotor : linkedMotors)
                linkedMotor.setDirection (direction);

        return this;
    }

    /**
     * This variable changes the rate at which adjustments are made to the wheels.  If it is off by a single tick in 100 ms, the power will then change by this value times the number of ticks off.
     */
    private double sensitivity = .00001;

    public NiFTMotorController setAdjustmentSensitivity (double sensitivity)
    {
        this.sensitivity = sensitivity;
        return this;
    }

    /**
     * Sometimes we want to prevent crazy errors where the encoder messes up count or something, but can't set the power to infinity all of a sudden.  So this prevents that.
     */
    private double sensitivityBound = .2;

    public NiFTMotorController setAdjustmentSensitivityBounds (double sensitivityBound)
    {
        this.sensitivityBound = sensitivityBound;
        return this;
    }

    /**
     * The rate at which adjustments to motor powers are made (in milliseconds)
     */
    private long refreshRate = 50;

    public NiFTMotorController setRefreshRate (long refreshRate)
    {
        this.refreshRate = refreshRate;
        return this;
    }

    /*---- ENCODER MANAGEMENT ----*/

    /**
     * Resets the encoder of the robot in a step by step fashion, ending up with the encoder at a count of zero but not changing robot steering (left to gyro and this pid)
     */
    private final void setMotorMode(DcMotor motor, DcMotor.RunMode runMode)
    {
        boolean doneSuccessfully = false;
        long additionalTime = 0;
        while (!doneSuccessfully)
        {
            try
            {
                motor.setMode (runMode);
                NiFTFlow.pauseForMS (100 + additionalTime);
                doneSuccessfully = true;
            } catch (Exception e)
            {
                if (e instanceof InterruptedException)
                    return;

                additionalTime += 20;
            }
        }
    }
    public void resetEncoder ()
    {
        // Used to set the encoder to the appropriate position.
        setMotorMode(encoderMotor, DcMotor.RunMode.RUN_USING_ENCODER);
        setMotorMode(encoderMotor, DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        setMotorMode(encoderMotor, DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    /*------ THREADING ------*/
    /**
     * This task is a simplistic way to call the update pid function rapidly but without having to change the autonomous files to include this during every loop.
     */
    public PIDTask pidInstance;
    private final class PIDTask extends NiFTSimpleTask
    {
        public PIDTask ()
        {
            super (pidTaskPackage, name + " PID Console");
        }

        @Override
        protected long onUpdate () throws InterruptedException
        {
            manuallyUpdatePID ();

            processConsole.updateWith (
                    "Current RPS conversion = " + rpsConversionFactor,
                    "Expected = " + getExpectedTicksSinceUpdate (),
                    "Actual = " + getActualTicksSinceUpdate ()
            );

            return refreshRate;
        }
    }

    public void setPIDStatus (boolean state)
    {
        pidInstance.active = state;
    }


    /******* PID STUFF *********/
    private double desiredRPS = 0;

    //Stored for each startEasyTask.
    private int previousMotorPosition;
    private long lastAdjustTime = 0;

    private double expectedTicksPerSecond;

    public void setRPS (double givenRPS)
    {
        //Will soon be modified by PID.
        desiredRPS = givenRPS;
        updateMotorPowers ();

        recordLastState ();

        expectedTicksPerSecond = motorType.encoderTicksPerRevolution * desiredRPS;
    }

    private double expectedTicksSinceUpdate, actualTicksSinceUpdate;
    public double getExpectedTicksSinceUpdate ()
    {
        return expectedTicksSinceUpdate;
    }
    public double getActualTicksSinceUpdate ()
    {
        return actualTicksSinceUpdate;
    }

    /**
     * This method calculates the number of ticks that should have occurred during the last some number of milliseconds, and then compares it with the actual value.  It then adjusts the rps conversion factor accordingly.
     */
    private void manuallyUpdatePID ()
    {
        if (lastAdjustTime != 0)
        {
            //Calculate expected and actual encoder ticks during last time frame.
            expectedTicksSinceUpdate = expectedTicksPerSecond * ((System.currentTimeMillis () - lastAdjustTime) / 1000.0);
            actualTicksSinceUpdate = encoderMotor.getCurrentPosition () - previousMotorPosition;

            //Add the appropriate adjustment factor the rps conversion factor.
            final double encoderAdjustment = (expectedTicksSinceUpdate - actualTicksSinceUpdate) * sensitivity;
            final double clippedAdjustment = Range.clip ((encoderAdjustment), -sensitivityBound, sensitivityBound);
            final double actualAdjustment = Math.signum (desiredRPS) * clippedAdjustment;

            //Don't allow it to increase the conversion factor beyond a conversion to a power of 1 (max for motor)
            if (!(actualAdjustment > 0 && rpsConversionFactor * desiredRPS > 1))
                rpsConversionFactor += actualAdjustment;

            updateMotorPowers ();
        }

        recordLastState ();
    }

    private void recordLastState ()
    {
        previousMotorPosition = encoderMotor.getCurrentPosition ();
        lastAdjustTime = System.currentTimeMillis ();
    }

    private void updateMotorPowers ()
    {
        //Set the initial power which the PID will soon modify.
        double desiredPower = Range.clip (desiredRPS * rpsConversionFactor, -1, 1);
        encoderMotor.setPower (desiredPower);
        if (linkedMotors != null)
            for (DcMotor linkedMotor : linkedMotors)
                linkedMotor.setPower (desiredPower);
    }

    //Used rarely but useful when required.
    public void setDirectMotorPower (double power)
    {
        double actualPower = Range.clip (power, -1, 1);
        encoderMotor.setPower (actualPower);
        if (linkedMotors != null)
            for (DcMotor linkedMotor : linkedMotors)
                linkedMotor.setPower (actualPower);
    }
}
