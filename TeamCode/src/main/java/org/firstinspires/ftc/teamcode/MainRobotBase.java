package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.enhancements.AdvancedMotorController;

public abstract class MainRobotBase extends ImprovedOpModeBase
{
    /*** CONFIGURE ALL ROBOT ELEMENTS HERE ***/
    //Drive motors (they are lists because it helps when we add on new motors.
    protected AdvancedMotorController leftDrive, rightDrive;
    //Other motors
    protected AdvancedMotorController harvester, flywheels;
    protected DcMotor lift;
    protected Servo rightButtonPusher, frontButtonPusher;
    protected Servo capBallHolder;
    protected final double CBH_CLOSED = 0.02, CBH_OPEN = 1.0;
    protected final double FBP_UP = 0.84, FBP_DOWN = FBP_UP - 0.63;

    protected void initializeHardware () throws InterruptedException
    {
        //Make sure that the robot components are found and initialized correctly.
        //This all happens during init()
        /*************************** DRIVING MOTORS ***************************/
        //The back motors are the ones that have functional encoders, while the front ones don't currently work.
        leftDrive = new AdvancedMotorController (
                initialize (DcMotor.class, "backLeft"), initialize (DcMotor.class, "frontLeft"),
                0.40,
                AdvancedMotorController.GearRatio.Two_To_One,
                AdvancedMotorController.MotorType.NeverRest40
        ).
                setMotorDirection (DcMotorSimple.Direction.REVERSE);

        rightDrive = new AdvancedMotorController (
                initialize (DcMotor.class, "backRight"), initialize (DcMotor.class, "frontRight"),
                0.36,
                AdvancedMotorController.GearRatio.Two_To_One,
                AdvancedMotorController.MotorType.NeverRest40
        );

        /*************************** OTHER MOTORS AND SERVOS ***************************/
        harvester = new AdvancedMotorController (
                initialize (DcMotor.class, "harvester"),
                0.40,
                AdvancedMotorController.GearRatio.Two_To_One,
                AdvancedMotorController.MotorType.NeverRest40
        ).
                setMotorDirection (DcMotorSimple.Direction.REVERSE);

        flywheels = new AdvancedMotorController (
                initialize (DcMotor.class, "flywheels"),
                0.02,
                AdvancedMotorController.GearRatio.One_to_One,
                AdvancedMotorController.MotorType.NeverRest3P7
        ).
                setMotorDirection (DcMotor.Direction.REVERSE).
                setAdjustmentSensitivity (.00001).
                setAdjustmentSensitivityBounds (0.3);

        lift = initialize (DcMotor.class, "lift");
        lift.setDirection (DcMotorSimple.Direction.REVERSE);

        rightButtonPusher = initialize (Servo.class, "rightButtonPusher");
        rightButtonPusher.setPosition (0.5); //The stop position for a continuous rotation servo.

        frontButtonPusher = initialize (Servo.class, "frontButtonPusher");
        frontButtonPusher.setPosition (FBP_UP);

        capBallHolder = initialize (Servo.class, "clamp");
        capBallHolder.setPosition (CBH_CLOSED);

        //Flywheels and harvester encoders.
        flywheels.resetEncoder ();
        harvester.resetEncoder ();
        flywheels.enablePeriodicPIDUpdates ();
        harvester.enablePeriodicPIDUpdates ();

        //Certain things are only applicable in autonomous or teleop.
        initializeOpModeSpecificHardware ();
    }

    //Optional overload.
    protected void initializeOpModeSpecificHardware () throws InterruptedException {}
}